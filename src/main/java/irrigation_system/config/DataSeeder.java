package irrigation_system.config;

import irrigation_system.model.IrrigationHistory;
import irrigation_system.model.IrrigationSchedule;
import irrigation_system.model.Parcel;
import irrigation_system.model.Recommendation;
import irrigation_system.model.User;
import irrigation_system.repository.IrrigationHistoryRepository;
import irrigation_system.repository.IrrigationScheduleRepository;
import irrigation_system.repository.ParcelRepository;
import irrigation_system.repository.RecommendationRepository;
import irrigation_system.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalTime;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedDatabase(
            UserRepository userRepository,
            ParcelRepository parcelRepository,
            IrrigationScheduleRepository irrigationScheduleRepository,
            IrrigationHistoryRepository irrigationHistoryRepository,
            RecommendationRepository recommendationRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            if (userRepository.findByEmail("bojan@example.com").isPresent()) {
                return;
            }

            User bojan = new User("Bojan Petrov", "bojan@example.com", passwordEncoder.encode("password123"));
            User ana = new User("Ana Stojanova", "ana@example.com", passwordEncoder.encode("password123"));

            userRepository.save(bojan);
            userRepository.save(ana);

            Parcel tomatoField = createParcel(
                    "Tomato Field",
                    "Skopje - Aerodrom",
                    2.5,
                    "Tomatoes",
                    LocalDate.of(2026, 4, 24),
                    "Needs regular morning irrigation during warm days.",
                    bojan
            );
            Parcel pepperGreenhouse = createParcel(
                    "Pepper Greenhouse",
                    "Skopje - Gazi Baba",
                    1.2,
                    "Peppers",
                    LocalDate.of(2026, 4, 26),
                    "Protected greenhouse parcel with drip irrigation.",
                    bojan
            );
            Parcel appleOrchard = createParcel(
                    "Apple Orchard",
                    "Tetovo",
                    4.8,
                    "Apples",
                    LocalDate.of(2026, 4, 22),
                    "Soil moisture drops quickly on windy days.",
                    ana
            );

            parcelRepository.save(tomatoField);
            parcelRepository.save(pepperGreenhouse);
            parcelRepository.save(appleOrchard);

            irrigationScheduleRepository.save(createSchedule(3, 120.0, true, tomatoField));
            irrigationScheduleRepository.save(createSchedule(2, 75.0, true, pepperGreenhouse));
            irrigationScheduleRepository.save(createSchedule(5, 250.0, false, appleOrchard));

            irrigationHistoryRepository.save(createHistory(LocalDate.of(2026, 4, 18), LocalTime.of(6, 30), 115.0, tomatoField));
            irrigationHistoryRepository.save(createHistory(LocalDate.of(2026, 4, 21), LocalTime.of(6, 45), 120.0, tomatoField));
            irrigationHistoryRepository.save(createHistory(LocalDate.of(2026, 4, 24), LocalTime.of(7, 0), 125.0, tomatoField));
            irrigationHistoryRepository.save(createHistory(LocalDate.of(2026, 4, 22), LocalTime.of(8, 15), 70.0, pepperGreenhouse));
            irrigationHistoryRepository.save(createHistory(LocalDate.of(2026, 4, 26), LocalTime.of(8, 0), 78.0, pepperGreenhouse));
            irrigationHistoryRepository.save(createHistory(LocalDate.of(2026, 4, 17), LocalTime.of(6, 0), 240.0, appleOrchard));
            irrigationHistoryRepository.save(createHistory(LocalDate.of(2026, 4, 22), LocalTime.of(6, 10), 255.0, appleOrchard));

            recommendationRepository.save(createRecommendation(true, "06:00 - 08:00", 130.0,
                    "Warm forecast and medium soil moisture. Irrigate early to reduce evaporation.",
                    LocalDate.of(2026, 4, 27), tomatoField));
            recommendationRepository.save(createRecommendation(false, "No irrigation needed", 0.0,
                    "Recent irrigation and controlled greenhouse conditions are enough for today.",
                    LocalDate.of(2026, 4, 27), pepperGreenhouse));
            recommendationRepository.save(createRecommendation(true, "05:30 - 07:00", 260.0,
                    "The orchard has not been irrigated for several days and needs deep watering.",
                    LocalDate.of(2026, 4, 27), appleOrchard));
        };
    }

    private Parcel createParcel(String name, String location, double size, String cropType,
                                LocalDate lastIrrigation, String notes, User user) {
        Parcel parcel = new Parcel();
        parcel.setName(name);
        parcel.setLocation(location);
        parcel.setSize(size);
        parcel.setCropType(cropType);
        parcel.setLastIrrigation(lastIrrigation);
        parcel.setNotes(notes);
        parcel.setUser(user);
        return parcel;
    }

    private IrrigationSchedule createSchedule(Integer intervalDays, double waterAmount, boolean active, Parcel parcel) {
        IrrigationSchedule schedule = new IrrigationSchedule();
        schedule.setIntervalDays(intervalDays);
        schedule.setWaterAmount(waterAmount);
        schedule.setActive(active);
        schedule.setParcel(parcel);
        return schedule;
    }

    private IrrigationHistory createHistory(LocalDate date, LocalTime time, double waterAmount, Parcel parcel) {
        IrrigationHistory history = new IrrigationHistory();
        history.setIrrigationDate(date);
        history.setIrrigationTime(time);
        history.setWaterAmount(waterAmount);
        history.setParcel(parcel);
        return history;
    }

    private Recommendation createRecommendation(boolean irrigationNeeded, String bestTime,
                                                double recommendedWaterAmount, String explanation,
                                                LocalDate createdAt, Parcel parcel) {
        Recommendation recommendation = new Recommendation();
        recommendation.setIrrigationNeeded(irrigationNeeded);
        recommendation.setBestTime(bestTime);
        recommendation.setRecommendedWaterAmount(recommendedWaterAmount);
        recommendation.setExplanation(explanation);
        recommendation.setCreatedAt(createdAt);
        recommendation.setParcel(parcel);
        return recommendation;
    }
}
