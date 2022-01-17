package com.example.api.config;

import com.google.inject.Guice;
import com.scalar.db.api.DistributedTransactionManager;
import com.scalar.db.config.DatabaseConfig;
import com.scalar.db.service.TransactionModule;
import com.scalar.db.service.TransactionService;
import java.io.IOException;
import java.net.URL;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class AppConfig {
  @Bean
  @Scope("singleton")
  DistributedTransactionManager createScalarDBTransactionManager() throws IOException {
    String databaseProp = "database.properties";
    DatabaseConfig scalarDBConfig =
        new DatabaseConfig(new URL("classpath:" + databaseProp).openConnection().getInputStream());
    return Guice.createInjector(new TransactionModule(scalarDBConfig))
        .getInstance(TransactionService.class);
  }
}
