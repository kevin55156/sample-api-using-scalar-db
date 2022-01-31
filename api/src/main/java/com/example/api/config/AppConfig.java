package com.example.api.config;

import com.scalar.db.api.DistributedTransactionManager;
import com.scalar.db.config.DatabaseConfig;
import com.scalar.db.service.TransactionFactory;
import java.io.IOException;
import java.net.URL;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.modelmapper.convention.NameTransformers;
import org.modelmapper.convention.NamingConventions;
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
    TransactionFactory factory = new TransactionFactory(scalarDBConfig);
    return factory.getTransactionManager();
  }

  @Bean
  public ModelMapper getModelMapper() {
    ModelMapper mapper = new ModelMapper();
    mapper
        .getConfiguration()
        .setDestinationNameTransformer(NameTransformers.builder())
        .setDestinationNamingConvention(NamingConventions.builder())
        .setMatchingStrategy(MatchingStrategies.STANDARD);
    return mapper;
  }
}
