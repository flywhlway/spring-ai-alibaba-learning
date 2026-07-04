package com.flywhl.saa.tool.model;

/**
 * 服务端标准时间快照：作为 {@code returnDirect=true} 工具的返回值，
 * 不经模型二次转述，直接原样序列化返回给调用方。
 *
 * @param city     查询城市（原样回显，未做时区换算，仅演示用途）
 * @param time     ISO-8601 本地时间字符串
 * @param timezone 服务端所在时区 ID
 * @author flywhl
 */
public record TimeVO(String city, String time, String timezone) {
}
