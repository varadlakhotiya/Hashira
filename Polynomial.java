import java.util.*;
import java.io.*;
import java.nio.file.*;

public class Polynomial {
    
    public static void main(String[] args) {
        try {
            // Read JSON content from files
            String json1 = readJsonFile("1.json");
            String json2 = readJsonFile("2.json");
            
            System.out.println("Test Case 1 Constant: " + findPolynomialConstant(json1));
            System.out.println("Test Case 2 Constant: " + findPolynomialConstant(json2));
        } catch (IOException e) {
            System.err.println("Error reading JSON files: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static String readJsonFile(String filename) throws IOException {
        try {
            // Try to read from current directory first
            return Files.readString(Paths.get(filename));
        } catch (IOException e) {
            // If not found in current directory, try relative path
            System.err.println("Could not find " + filename + " in current directory");
            throw e;
        }
    }
    
    public static long findPolynomialConstant(String jsonString) {
        Map<String, Object> jsonData = parseJson(jsonString);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> keys = (Map<String, Object>) jsonData.get("keys");
        int k = Integer.parseInt(keys.get("k").toString());
        
        List<Point> points = new ArrayList<>();
        
        // Parse all points
        for (Map.Entry<String, Object> entry : jsonData.entrySet()) {
            String key = entry.getKey();
            if (key.equals("keys")) continue;
            
            try {
                int x = Integer.parseInt(key);
                @SuppressWarnings("unchecked")
                Map<String, Object> pointData = (Map<String, Object>) entry.getValue();
                int base = Integer.parseInt(pointData.get("base").toString());
                String value = pointData.get("value").toString();
                
                long y = convertToDecimal(value, base);
                points.add(new Point(x, y));
            } catch (NumberFormatException e) {
                // Skip non-numeric keys
                continue;
            }
        }
        
        // Sort points by x-coordinate and take first k points
        points.sort((a, b) -> Integer.compare(a.x, b.x));
        List<Point> selectedPoints = points.subList(0, k);
        
        // Use Lagrange interpolation to find polynomial value at x=0 (constant term)
        return lagrangeInterpolation(selectedPoints, 0);
    }
    
    private static Map<String, Object> parseJson(String json) {
        Map<String, Object> result = new HashMap<>();
        
        // Remove whitespace and outer braces
        json = json.trim().replaceAll("\\s+", "");
        json = json.substring(1, json.length() - 1);
        
        // Split by commas, but be careful about nested structures
        List<String> tokens = splitJsonTokens(json);
        
        for (String token : tokens) {
            String[] keyValue = splitKeyValue(token);
            if (keyValue.length == 2) {
                String key = keyValue[0].replaceAll("\"", "");
                String value = keyValue[1];
                
                if (value.startsWith("{")) {
                    // Nested object
                    result.put(key, parseJson(value));
                } else {
                    // Simple value
                    result.put(key, value.replaceAll("\"", ""));
                }
            }
        }
        
        return result;
    }
    
    private static List<String> splitJsonTokens(String json) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int braceCount = 0;
        boolean inQuotes = false;
        
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (!inQuotes) {
                if (c == '{') {
                    braceCount++;
                } else if (c == '}') {
                    braceCount--;
                } else if (c == ',' && braceCount == 0) {
                    tokens.add(current.toString());
                    current = new StringBuilder();
                    continue;
                }
            }
            
            current.append(c);
        }
        
        if (current.length() > 0) {
            tokens.add(current.toString());
        }
        
        return tokens;
    }
    
    private static String[] splitKeyValue(String token) {
        int colonIndex = -1;
        boolean inQuotes = false;
        int braceCount = 0;
        
        for (int i = 0; i < token.length(); i++) {
            char c = token.charAt(i);
            
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (!inQuotes) {
                if (c == '{') {
                    braceCount++;
                } else if (c == '}') {
                    braceCount--;
                } else if (c == ':' && braceCount == 0) {
                    colonIndex = i;
                    break;
                }
            }
        }
        
        if (colonIndex != -1) {
            return new String[]{
                token.substring(0, colonIndex),
                token.substring(colonIndex + 1)
            };
        }
        
        return new String[]{token};
    }
    
    private static long convertToDecimal(String value, int base) {
        long result = 0;
        long power = 1;
        
        // Convert from right to left
        for (int i = value.length() - 1; i >= 0; i--) {
            char digit = value.charAt(i);
            int digitValue;
            
            if (digit >= '0' && digit <= '9') {
                digitValue = digit - '0';
            } else if (digit >= 'a' && digit <= 'z') {
                digitValue = digit - 'a' + 10;
            } else if (digit >= 'A' && digit <= 'Z') {
                digitValue = digit - 'A' + 10;
            } else {
                throw new IllegalArgumentException("Invalid digit: " + digit);
            }
            
            if (digitValue >= base) {
                throw new IllegalArgumentException("Digit " + digit + " is invalid for base " + base);
            }
            
            result += digitValue * power;
            power *= base;
        }
        
        return result;
    }
    
    private static long lagrangeInterpolation(List<Point> points, int x) {
        long result = 0;
        int n = points.size();
        
        for (int i = 0; i < n; i++) {
            long term = points.get(i).y;
            
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    term *= (x - points.get(j).x);
                    term /= (points.get(i).x - points.get(j).x);
                }
            }
            
            result += term;
        }
        
        return result;
    }
    
    static class Point {
        int x;
        long y;
        
        Point(int x, long y) {
            this.x = x;
            this.y = y;
        }
        
        @Override
        public String toString() {
            return "(" + x + ", " + y + ")";
        }
    }
}