package service.saju_taro_service.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

@Configuration
public class FirebaseConfig {

    @Value("${firebase.credentials-path}")
    private String credentialsPath;

    @PostConstruct
    public void init() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(
                            new ClassPathResource(credentialsPath).getInputStream()))
                    .build();
            FirebaseApp.initializeApp(options);
            System.out.println("✅ Firebase App Initialized successfully");
        }
    }

    /** ✅ FirebaseMessaging Bean 등록 */
    @Bean
    public FirebaseMessaging firebaseMessaging() {
        return FirebaseMessaging.getInstance(FirebaseApp.getInstance());
    }
}
