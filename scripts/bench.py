#!/usr/bin/env python3
"""
锐客 CRM 接口压测脚本（纯标准库，无需安装依赖）

用法：
    python scripts/bench.py                       # 默认 20 并发 x 50 次
    python scripts/bench.py -c 50 -n 100          # 50 并发 x 100 次
    python scripts/bench.py --paths /clues,/customers,/dashboard/stats

输出：QPS、平均延迟、P50/P95/P99、错误率，可直接作为压测报告数据。
"""

import argparse
import json
import statistics
import sys
import threading
import time
import urllib.error
import urllib.request

DEFAULT_PATHS = ["/clues?page=1&pageSize=10", "/customers?page=1&pageSize=10",
                 "/businesses?page=1&pageSize=10", "/dashboard/stats"]


def login(base, username, password):
    body = json.dumps({"username": username, "password": password}).encode()
    req = urllib.request.Request(base + "/login", data=body,
                                 headers={"Content-Type": "application/json"})
    with urllib.request.urlopen(req, timeout=10) as resp:
        data = json.load(resp)
    token = data.get("data", {}).get("token")
    if not token:
        raise RuntimeError("登录失败：" + str(data))
    return token


def one_request(url, token):
    """返回 (耗时毫秒, HTTP 状态码)"""
    req = urllib.request.Request(url, headers={"token": token})
    start = time.perf_counter()
    try:
        with urllib.request.urlopen(req, timeout=15) as resp:
            resp.read()
            code = resp.status
    except urllib.error.HTTPError as e:
        code = e.code
    except Exception:
        code = -1
    return (time.perf_counter() - start) * 1000, code


def worker(base, token, paths, rounds, latencies, errors, lock):
    for i in range(rounds):
        path = paths[i % len(paths)]
        cost, code = one_request(base + path, token)
        with lock:
            latencies.append(cost)
            if code != 200:
                errors.append(code)


def percentile(sorted_values, p):
    if not sorted_values:
        return 0.0
    k = (len(sorted_values) - 1) * (p / 100.0)
    lo, hi = int(k), min(int(k) + 1, len(sorted_values) - 1)
    return sorted_values[lo] + (sorted_values[hi] - sorted_values[lo]) * (k - lo)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--base", default="http://localhost:8080")
    parser.add_argument("-c", "--concurrency", type=int, default=20)
    parser.add_argument("-n", "--rounds", type=int, default=50, help="每个线程的请求次数")
    parser.add_argument("--username", default="zhangsan")
    parser.add_argument("--password", default="zhangsan123")
    parser.add_argument("--paths", default=",".join(DEFAULT_PATHS))
    parser.add_argument("--tag", default="", help="报告标题里的场景标记")
    args = parser.parse_args()

    paths = [p.strip() for p in args.paths.split(",") if p.strip()]
    total = args.concurrency * args.rounds

    print(f"目标 {args.base}｜并发 {args.concurrency}｜每人 {args.rounds} 次｜总请求 {total}")
    token = login(args.base, args.username, args.password)
    print("已登录，开始压测…")

    latencies, errors = [], []
    lock = threading.Lock()
    threads = [
        threading.Thread(target=worker,
                         args=(args.base, token, paths, args.rounds, latencies, errors, lock))
        for _ in range(args.concurrency)
    ]

    start = time.perf_counter()
    for t in threads:
        t.start()
    for t in threads:
        t.join()
    elapsed = time.perf_counter() - start

    latencies.sort()
    ok = len(latencies) - len(errors)
    print("\n" + "=" * 52)
    print("压测结果" + (f"（{args.tag}）" if args.tag else ""))
    print("=" * 52)
    print(f"总耗时      {elapsed:.2f} s")
    print(f"成功请求    {ok} / {len(latencies)}")
    print(f"QPS         {ok / elapsed:.1f}")
    print(f"平均延迟    {statistics.mean(latencies):.1f} ms")
    print(f"P50         {percentile(latencies, 50):.1f} ms")
    print(f"P95         {percentile(latencies, 95):.1f} ms")
    print(f"P99         {percentile(latencies, 99):.1f} ms")
    print(f"最大延迟    {latencies[-1]:.1f} ms")
    print(f"错误率      {len(errors) / len(latencies) * 100:.2f}%")
    if errors:
        from collections import Counter
        print(f"错误分布    {dict(Counter(errors))}")
    print("=" * 52)

    return 0 if not errors else 1


if __name__ == "__main__":
    sys.exit(main())
