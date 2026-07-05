package com.example.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Rails の SolidQueue (ActiveJob) に相当する非同期処理を有効化する。
 * SuggestionGenerateService#generate は @Async で実行される。
 */
@Configuration
@EnableAsync
public class AsyncConfig {
}