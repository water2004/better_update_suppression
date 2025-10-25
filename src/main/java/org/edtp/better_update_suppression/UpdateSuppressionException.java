package org.edtp.better_update_suppression;

/**
 * 这是一个自定义的运行时异常，专门用于在方块更新逻辑中
 * 安全地中断更新链，而不会导致游戏崩溃。
 */
public class UpdateSuppressionException extends RuntimeException {
    public UpdateSuppressionException(String message) {
        super(message);
    }
}