package com.flywhl.saa.common.result;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link Result} 与 {@link PageResult} 单元测试。
 *
 * @author flywhl
 */
class ResultTest {

    @Test
    @DisplayName("ok() 返回 code=0 且 isSuccess 为 true")
    void okShouldReturnSuccess() {
        Result<String> result = Result.ok("hello");
        assertThat(result.code()).isZero();
        assertThat(result.data()).isEqualTo("hello");
        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    @DisplayName("fail() 携带错误码与覆盖信息")
    void failShouldCarryCodeAndMessage() {
        Result<Void> result = Result.fail(CommonResultCode.BAD_REQUEST, "name 不能为空");
        assertThat(result.code()).isEqualTo(1000);
        assertThat(result.message()).isEqualTo("name 不能为空");
        assertThat(result.isSuccess()).isFalse();
    }

    @Test
    @DisplayName("PageResult 正确计算总页数")
    void pageResultShouldComputeTotalPages() {
        PageResult<Integer> page = PageResult.of(1, 10, 25, List.of(1, 2, 3));
        assertThat(page.totalPages()).isEqualTo(3);
        assertThat(PageResult.empty(1, 10).totalPages()).isZero();
    }
}
