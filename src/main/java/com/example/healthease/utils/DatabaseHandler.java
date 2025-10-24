package com.example.healthease.utils;

import java.sql.*;

public class DatabaseHandler {
    private static final String DB_URL = "jdbc:sqlite:HealthEase.db";

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to load SQLite JDBC driver", e);
        }
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("PRAGMA foreign_keys = ON");

            // Create users table
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "username TEXT UNIQUE NOT NULL," +
                    "password TEXT NOT NULL," +
                    "email TEXT UNIQUE NOT NULL," +
                    "role TEXT NOT NULL)");

            // Create messages table
            stmt.execute("CREATE TABLE IF NOT EXISTS messages (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "sender_id INTEGER NOT NULL," +
                    "receiver_id INTEGER NOT NULL," +
                    "content TEXT NOT NULL," +
                    "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY(sender_id) REFERENCES users(id)," +
                    "FOREIGN KEY(receiver_id) REFERENCES users(id))");

            // Create health_tips table
            stmt.execute("CREATE TABLE IF NOT EXISTS health_tips (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER NOT NULL," +
                    "tip TEXT NOT NULL," +
                    "FOREIGN KEY(user_id) REFERENCES users(id))");

            // Create meal_plans table
            stmt.execute("CREATE TABLE IF NOT EXISTS meal_plans (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER NOT NULL," +
                    "breakfast TEXT," +
                    "lunch TEXT," +
                    "dinner TEXT," +
                    "snacks TEXT," +
                    "FOREIGN KEY(user_id) REFERENCES users(id))");

            // Create FAQs table for Health Tips (question + answer)
            stmt.execute("CREATE TABLE IF NOT EXISTS faqs (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "question TEXT UNIQUE NOT NULL," +
                    "answer TEXT NOT NULL)");

            // Insert default users
            stmt.execute("INSERT OR IGNORE INTO users (username, password, email, role) VALUES " +
                    "('admin', 'admin123', 'admin@healthease.com', 'admin')," +
                    "('john', 'password1', 'john.doe@example.com', 'patient')," +
                    "('drsara', 'password2', 'sara.lee@example.com', 'doctor')");

            // Migrate legacy usernames if present (keep the same passwords)
            try { stmt.executeUpdate("UPDATE users SET username='john', email='john.doe@example.com', role='patient' WHERE username='user1' AND password='password1'"); } catch (SQLException ignore) {}
            try { stmt.executeUpdate("UPDATE users SET username='drsara', email='sara.lee@example.com', role='doctor' WHERE username='user2' AND password='password2'"); } catch (SQLException ignore) {}

            // Insert sample health tips
            stmt.execute("INSERT OR IGNORE INTO health_tips (user_id, tip) VALUES " +
                    "(1, 'Drink at least 8 glasses of water daily')," +
                    "(1, 'Get 7-8 hours of sleep every night')," +
                    "(1, 'Take a 30-minute walk daily')," +
                    "(2, 'Reduce sugar intake to less than 25g per day')," +
                    "(2, 'Include more vegetables in your meals')");

            // Insert sample meal plans
            stmt.execute("INSERT OR IGNORE INTO meal_plans (user_id, breakfast, lunch, dinner, snacks) VALUES " +
                    "(1, 'Oatmeal with berries', 'Grilled chicken salad', 'Salmon with steamed vegetables', 'Apple with almond butter')," +
                    "(2, 'Greek yogurt with nuts', 'Quinoa bowl with vegetables', 'Turkey stir-fry with brown rice', 'Handful of almonds')");

            // Insert default FAQs (20 entries)
            stmt.execute("INSERT OR IGNORE INTO faqs (question, answer) VALUES " +
                    "('How do I start exercising safely as a beginner?', 'Start with 10â€“20 minutes of brisk walking 3â€“4x/week. Add light bodyweight moves (squats, wall push-ups, glute bridges). Focus on form; increase time ~10%/week.')," +
                    "('What is a balanced plate for meals?', 'Half non-starchy veg, quarter lean protein (chicken, fish, tofu, beans), quarter whole grains or starchy veg, plus water. Add fruit or yogurt if hungry.')," +
                    "('Tips to lose weight sustainably?', 'Small deficit (300â€“500 kcal/day). Prioritize protein and fiber, limit sugary drinks, walk 8â€“10k steps, and strength train 2x/week.')," +
                    "('How much water should I drink?', '6â€“8 glasses/day as a practical target; more in heat or activity. Use urine color (pale straw) as a guide.')," +
                    "('I sit all day. How can I stay active?', 'Stand/move 2â€“3 minutes every 60â€“90 minutes, take short walks after meals, include desk stretches and an evening walk.')," +
                    "('How many hours of sleep do adults need?', 'Most adults do best with 7â€“9 hours. Keep a consistent schedule, limit screens 60 minutes preâ€‘bed, and keep your room dark, cool, and quiet.')," +
                    "('Healthy snack ideas?', 'Greek yogurt with berries, fruit and nuts, hummus with carrots, boiled eggs, wholeâ€‘grain crackers with cheese, or a protein smoothie.')," +
                    "('How often should I strength train?', '2â€“3 nonconsecutive days/week. Aim for about 6â€“10 sets per muscle group weekly. Cover push, pull, legs, and core.')," +
                    "('Cardio vs. strengthâ€”what mix is good?', '150 min/week moderate cardio (or 75 min vigorous) plus 2+ strength sessions. Mix depends on goals; combine both for health.')," +
                    "('Any tips to manage stress?', 'Try 4â€‘7â€‘8 breathing 3â€“5 minutes, short walks, sunlight exposure, and limit late caffeine. Keep a simple windâ€‘down routine.')," +
                    "('Breakfast ideas for energy?', 'Oats + milk + nuts/fruit; eggs with wholeâ€‘grain toast and veggies; Greek yogurt parfait; smoothie with fruit, spinach, and protein.')," +
                    "('How to build a daily movement habit?', 'Attach it to a routine (after brushing teeth, take a 10â€‘min walk). Keep it small and consistent; track streaks.')," +
                    "('How to avoid injury when working out?', 'Warm up 5â€“10 min, progress gradually (~10%/week), focus on technique, and rest when sore. If sharp pain persists, consult a professional.')," +
                    "('Whatâ€™s a good preâ€‘workout snack?', '1â€“2 hours before: fruit + yogurt, toast with peanut butter, or a small rice bowl with chicken. Keep it light if training soon.')," +
                    "('How much protein do I need?', 'General guidance: 1.2â€“2.0 g/kg/day depending on activity and goals. Spread across meals for satiety and muscle support.')," +
                    "('How to cut down on sugar?', 'Swap sugary drinks for water/sparkling water, choose fruit over sweets, read labels, and schedule desserts (e.g., 1â€“2x/week).')," +
                    "('Best way to track progress?', 'Pick 2â€“3 metrics: energy, sleep quality, steps, workout consistency, or waist measurement. Review weekly; look for trends, not perfection.')," +
                    "('Healthy lunchbox ideas?', 'Wholeâ€‘grain wrap with chicken/tofu and veggies; rice + beans + salsa + avocado; quinoa salad with chickpeas and roasted veggies.')," +
                    "('How to stay motivated long term?', 'Set small process goals (e.g., walk 15 min daily), celebrate consistency, plan for imperfect days, and make it enjoyable (music, buddy, outdoors).')," +
                    "('Simple home workout plan?', '3x/week: 3 rounds of squats, pushâ€‘ups (or wall), rows (band/backpack), hip hinges, and planks. 8â€“15 reps each; rest ~1 minute between moves.')");

            System.out.println("Database initialized successfully with all tables.");

        } catch (SQLException e) {
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }
}
