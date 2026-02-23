package com.appverse.app_service;



import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import com.appverse.app_service.model.Application;
import com.appverse.app_service.repository.ApplicationRepository;

@DataMongoTest
public class ApplicationRepositoryTest {

    @Autowired
    private ApplicationRepository repository;

    @Test
    void save_and_find() {
        Application app = new Application();
        app.setName("Test");

        Application saved = repository.save(app);

        assertThat(repository.findById(saved.getId()).isPresent());
    }
    
}
