package com.flywhl.saa.common.result;

import java.util.List;
import java.util.Objects;

/**
 * 统一分页响应体（SSOT）。
 *
 * <p>与 {@link Result} 组合使用：{@code Result<PageResult<UserVO>>}。
 *
 * @param pageNo   页码，从 1 开始
 * @param pageSize 每页大小
 * @param total    总记录数
 * @param records  当前页记录
 * @param <T>      记录类型
 * @author flywhl
 * @since 1.0.0
 */
public record PageResult<T>(long pageNo, long pageSize, long total, List<T> records) {

    public PageResult {
        Objects.requireNonNull(records, "records must not be null");
    }

    /**
     * 构建分页结果。
     *
     * @param pageNo   页码（从 1 开始）
     * @param pageSize 每页大小
     * @param total    总记录数
     * @param records  当前页记录
     * @param <T>      记录类型
     * @return 分页结果
     */
    public static <T> PageResult<T> of(long pageNo, long pageSize, long total, List<T> records) {
        return new PageResult<>(pageNo, pageSize, total, records);
    }

    /**
     * 空页。
     *
     * @param pageNo   页码
     * @param pageSize 每页大小
     * @param <T>      记录类型
     * @return 空分页结果
     */
    public static <T> PageResult<T> empty(long pageNo, long pageSize) {
        return new PageResult<>(pageNo, pageSize, 0, List.of());
    }

    /**
     * 总页数。
     *
     * @return 总页数
     */
    public long totalPages() {
        return pageSize == 0 ? 0 : (total + pageSize - 1) / pageSize;
    }
}
