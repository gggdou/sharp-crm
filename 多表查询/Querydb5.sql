# select * from tb_emp, tb_dept

# 隐式内连接
select * from tb_emp, tb_dept where tb_emp.dept_id = tb_dept.id;

# 显示内连接
select * from tb_emp inner join tb_dept on tb_emp.dept_id = tb_dept.id;


# 左外连接
select tb_emp.name, tb_dept.name
from tb_emp
left outer join tb_dept on tb_emp.dept_id = tb_dept.id;


# 查询平均薪资
select avg(tb_emp.salary) from tb_emp;

# 查询大于平均薪资的人
SELECT tb_emp.name , tb_emp.salary FROM tb_emp WHERE salary > (SELECT AVG(tb_emp.salary) FROM tb_emp);

# 查询部门薪资
select tb_emp.name , tb_emp.salary
from tb_emp
where tb_emp.dept_id
          in (select tb_dept.id from tb_dept where tb_dept.name in ('学工部','教研部'));

select tb_emp.name
from tb_emp
where tb_emp.dept_id
          in (select tb_dept.id from tb_dept where tb_dept.name in ('学工部','教研部'));


# 表子查询
# 查询薪资高于本部门的的人的信息

# 1. 查询每个部门的平均薪资
select dept_id, avg(salary)
from tb_emp
group by dept_id;

# 2.查询当前员工的薪资是否大于本部门的平均薪资
select name,salary,dept_id
from tb_emp
where salary > (select t1.avg from (select dept_id, avg(salary) avg
                 from tb_emp
                 group by dept_id) t1 where t1.dept_id = tb_emp.dept_id) order by dept_id