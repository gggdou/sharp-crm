package cn.itcast.mapper;

import cn.itcast.dto.OperateLogDto;
import cn.itcast.pojo.OperateLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LogMapper extends BaseMapper<OperateLog> {

    IPage<OperateLog> findByPage(IPage<OperateLog> page, OperateLogDto operateLogDto);
}
