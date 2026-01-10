package com.framework.utils;

import com.github.javafaker.Faker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Test data generation utilities using JavaFaker.
 */
public final class TestDataGenerator {

    private static final Logger log = LogManager.getLogger(TestDataGenerator.class);
    private static final Faker faker = new Faker();
    private static final Random random = new Random();

    private TestDataGenerator() {
    }

    // ==================== Personal Data ====================

    public static String firstName() {
        return faker.name().firstName();
    }

    public static String lastName() {
        return faker.name().lastName();
    }

    public static String fullName() {
        return faker.name().fullName();
    }

    public static String username() {
        return faker.name().username();
    }

    public static String email() {
        return faker.internet().emailAddress();
    }

    public static String email(String domain) {
        return faker.name().username().toLowerCase() + "@" + domain;
    }

    public static String password() {
        return faker.internet().password(8, 16, true, true, true);
    }

    public static String password(int minLength, int maxLength) {
        return faker.internet().password(minLength, maxLength, true, true);
    }

    public static String phoneNumber() {
        return faker.phoneNumber().phoneNumber();
    }

    public static String cellPhone() {
        return faker.phoneNumber().cellPhone();
    }

    // ==================== Address Data ====================

    public static String streetAddress() {
        return faker.address().streetAddress();
    }

    public static String city() {
        return faker.address().city();
    }

    public static String state() {
        return faker.address().state();
    }

    public static String zipCode() {
        return faker.address().zipCode();
    }

    public static String country() {
        return faker.address().country();
    }

    public static Map<String, String> fullAddress() {
        Map<String, String> address = new LinkedHashMap<>();
        address.put("street", streetAddress());
        address.put("city", city());
        address.put("state", state());
        address.put("zipCode", zipCode());
        address.put("country", country());
        return address;
    }

    // ==================== Financial Data ====================

    public static String creditCardNumber() {
        return faker.finance().creditCard();
    }

    public static String creditCardNumber(String type) {
        return faker.finance().creditCard();
    }

    public static String cvv() {
        return String.format("%03d", random.nextInt(1000));
    }

    public static String expiryDate() {
        int month = random.nextInt(12) + 1;
        int year = LocalDate.now().getYear() + random.nextInt(5) + 1;
        return String.format("%02d/%d", month, year % 100);
    }

    public static String iban() {
        return faker.finance().iban();
    }

    public static String bic() {
        return faker.finance().bic();
    }

    // ==================== Date/Time Data ====================

    public static LocalDate pastDate(int maxDaysBack) {
        return LocalDate.now().minusDays(random.nextInt(maxDaysBack) + 1);
    }

    public static LocalDate futureDate(int maxDaysAhead) {
        return LocalDate.now().plusDays(random.nextInt(maxDaysAhead) + 1);
    }

    public static LocalDate birthDate(int minAge, int maxAge) {
        int age = random.nextInt(maxAge - minAge + 1) + minAge;
        return LocalDate.now().minusYears(age).minusDays(random.nextInt(365));
    }

    public static String formattedDate(LocalDate date, String pattern) {
        return date.format(DateTimeFormatter.ofPattern(pattern));
    }

    public static String timestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    // ==================== Text Data ====================

    public static String sentence() {
        return faker.lorem().sentence();
    }

    public static String sentence(int wordCount) {
        return faker.lorem().sentence(wordCount);
    }

    public static String paragraph() {
        return faker.lorem().paragraph();
    }

    public static String paragraph(int sentenceCount) {
        return faker.lorem().paragraph(sentenceCount);
    }

    public static List<String> words(int count) {
        return faker.lorem().words(count);
    }

    public static String characters(int count) {
        return faker.lorem().characters(count);
    }

    // ==================== Company Data ====================

    public static String companyName() {
        return faker.company().name();
    }

    public static String industry() {
        return faker.company().industry();
    }

    public static String catchPhrase() {
        return faker.company().catchPhrase();
    }

    public static String jobTitle() {
        return faker.job().title();
    }

    // ==================== Internet Data ====================

    public static String url() {
        return faker.internet().url();
    }

    public static String domainName() {
        return faker.internet().domainName();
    }

    public static String ipAddress() {
        return faker.internet().ipV4Address();
    }

    public static String macAddress() {
        return faker.internet().macAddress();
    }

    public static String userAgent() {
        return faker.internet().userAgentAny();
    }

    // ==================== Unique Identifiers ====================

    public static String uuid() {
        return UUID.randomUUID().toString();
    }

    public static String uniqueId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    public static String uniqueId(String prefix) {
        return prefix + "_" + timestamp() + "_" + randomAlphanumeric(4);
    }

    // ==================== Random Generators ====================

    public static int randomInt(int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }

    public static double randomDouble(double min, double max) {
        return min + (max - min) * random.nextDouble();
    }

    public static String randomAlphanumeric(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public static String randomAlpha(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public static String randomNumeric(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    public static <T> T randomFrom(List<T> list) {
        if (list == null || list.isEmpty()) return null;
        return list.get(random.nextInt(list.size()));
    }

    @SafeVarargs
    public static <T> T randomFrom(T... items) {
        if (items == null || items.length == 0) return null;
        return items[random.nextInt(items.length)];
    }

    public static boolean randomBoolean() {
        return random.nextBoolean();
    }

    public static boolean randomBoolean(double trueProbability) {
        return random.nextDouble() < trueProbability;
    }

    // ==================== App-Specific Data ====================

    public static Map<String, String> validLoginCredentials() {
        Map<String, String> credentials = new LinkedHashMap<>();
        credentials.put("username", "standard_user");
        credentials.put("password", "secret_sauce");
        return credentials;
    }

    public static Map<String, String> invalidLoginCredentials() {
        Map<String, String> credentials = new LinkedHashMap<>();
        credentials.put("username", "invalid_user_" + randomAlphanumeric(4));
        credentials.put("password", "wrong_password_" + randomAlphanumeric(4));
        return credentials;
    }

    public static Map<String, String> newUserData() {
        Map<String, String> user = new LinkedHashMap<>();
        user.put("firstName", firstName());
        user.put("lastName", lastName());
        user.put("email", email());
        user.put("phone", cellPhone());
        user.put("password", password());
        return user;
    }

    // ==================== Builder Pattern ====================

    /**
     * Creates a test data builder for customizable data generation.
     */
    public static TestDataBuilder builder() {
        return new TestDataBuilder();
    }

    public static class TestDataBuilder {
        private final Map<String, String> data = new LinkedHashMap<>();

        public TestDataBuilder with(String key, String value) {
            data.put(key, value);
            return this;
        }

        public TestDataBuilder withName() {
            data.put("firstName", firstName());
            data.put("lastName", lastName());
            return this;
        }

        public TestDataBuilder withEmail() {
            data.put("email", email());
            return this;
        }

        public TestDataBuilder withPhone() {
            data.put("phone", cellPhone());
            return this;
        }

        public TestDataBuilder withAddress() {
            data.putAll(fullAddress());
            return this;
        }

        public TestDataBuilder withCredentials() {
            data.put("username", username());
            data.put("password", password());
            return this;
        }

        public Map<String, String> build() {
            return new LinkedHashMap<>(data);
        }
    }
}

