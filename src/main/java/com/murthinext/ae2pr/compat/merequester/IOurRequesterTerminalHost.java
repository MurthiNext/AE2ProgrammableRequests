package com.murthinext.ae2pr.compat.merequester;

import com.murthinext.ae2pr.requester.RedstoneRequesterBlockEntity;

/**
 * 由 {@link RequesterTerminalMenuMixin} 实现的鸭子接口，
 * 供 {@link AbstractRequesterMenuMixin} 通过 {@code id} 解析本模组的红石请求器。
 */
public interface IOurRequesterTerminalHost {

    RedstoneRequesterBlockEntity ae2pr$getOurRequester(long id);
}
