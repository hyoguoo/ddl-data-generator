package hyoguoo.ddl_data_generator.executor;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import net.datafaker.Faker;
import org.springframework.stereotype.Component;

@Component
public class GeneratorRegistry {

    private final Faker faker = new Faker(new Random());

    public ColumnGenerator build(String gen, Map<String, Object> args) {
        return switch (gen.toLowerCase()) {
            case "const" -> () -> args.get("value");
            case "uuid" -> () -> UUID.randomUUID().toString();
            case "name" -> () -> faker.name().fullName();
            case "email" -> {
                String domain = String.valueOf(args.getOrDefault("domain", "example.com"));
                yield () -> faker.internet().emailAddress().replaceAll("@.*$", "@" + domain);
            }
            case "lorem" -> {
                int words = ((Number) args.getOrDefault("words", 8)).intValue();
                int maxLen = ((Number) args.getOrDefault("maxLen", 255)).intValue();
                yield () -> {
                    String s = faker.lorem().sentence(words);
                    return s.length() <= maxLen ? s : s.substring(0, maxLen);
                };
            }
            case "int" -> {
                int min = ((Number) args.getOrDefault("min", 0)).intValue();
                int max = ((Number) args.getOrDefault("max", 1_000_000)).intValue();
                yield () -> ThreadLocalRandom.current().nextInt(min, max + 1);
            }
            case "decimal" -> {
                long min = ((Number) args.getOrDefault("min", 0)).longValue();
                long max = ((Number) args.getOrDefault("max", 1_000_000)).longValue();
                int scale = ((Number) args.getOrDefault("scale", 2)).intValue();
                yield () -> BigDecimal.valueOf(ThreadLocalRandom.current().nextLong(min, max + 1), scale);
            }
            case "datetime" -> {
                String from = String.valueOf(args.getOrDefault("from", "2020-01-01"));
                String to = String.valueOf(args.getOrDefault("to", "now"));
                long start = Timestamp.valueOf(from + " 00:00:00").getTime();
                long end = "now".equalsIgnoreCase(to) ? System.currentTimeMillis()
                        : Timestamp.valueOf(to + " 23:59:59").getTime();
                yield () -> new Timestamp(ThreadLocalRandom.current().nextLong(start, end));
            }
            case "pick" -> {
                @SuppressWarnings("unchecked")
                List<Object> values = (List<Object>) args.get("values");
                @SuppressWarnings("unchecked")
                List<Number> weights = (List<Number>) args.getOrDefault("weights",
                        Collections.nCopies(values.size(), 1));
                double[] w = weights.stream().mapToDouble(Number::doubleValue).toArray();
                double sum = Arrays.stream(w).sum();
                for (int i = 0; i < w.length; i++) {
                    w[i] /= sum;
                }
                yield () -> {
                    double r = ThreadLocalRandom.current().nextDouble();
                    double acc = 0;
                    for (int i = 0; i < w.length; i++) {
                        acc += w[i];
                        if (r <= acc) {
                            return values.get(i);
                        }
                    }
                    return values.get(values.size() - 1);
                };
            }
            case "auto" -> () -> null;
            default -> throw new IllegalArgumentException("unknown gen: " + gen);
        };
    }

    interface ColumnGenerator {

        Object next();
    }
}