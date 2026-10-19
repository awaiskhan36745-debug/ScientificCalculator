package com.example.scientificcalculator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class ExpressionEvaluator {

    private boolean isDegree = true;

    public void setDegree(boolean degree) {
        isDegree = degree;
    }

    public double evaluate(String expr) throws Exception {
        if (expr == null || expr.trim().isEmpty()) {
            return 0.0;
        }
        // Normalize expression symbols
        expr = expr.replace("×", "*")
                   .replace("÷", "/")
                   .replace("π", String.valueOf(Math.PI))
                   .replace("e", String.valueOf(Math.E));

        List<String> tokens = tokenize(expr);
        List<String> rpn = infixToPostfix(tokens);
        return evaluateRPN(rpn);
    }

    private List<String> tokenize(String expr) {
        List<String> tokens = new ArrayList<>();
        int i = 0;
        while (i < expr.length()) {
            char c = expr.charAt(i);
            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }

            // Numbers (including decimals)
            if (Character.isDigit(c) || c == '.') {
                StringBuilder sb = new StringBuilder();
                while (i < expr.length() && (Character.isDigit(expr.charAt(i)) || expr.charAt(i) == '.')) {
                    sb.append(expr.charAt(i));
                    i++;
                }
                tokens.add(sb.toString());
                continue;
            }

            // Functions or identifiers (sin, cos, tan, log, ln, sqrt, abs, etc.)
            if (Character.isLetter(c)) {
                StringBuilder sb = new StringBuilder();
                while (i < expr.length() && Character.isLetter(expr.charAt(i))) {
                    sb.append(expr.charAt(i));
                    i++;
                }
                tokens.add(sb.toString().toLowerCase());
                continue;
            }

            // Operators and parentheses
            if (c == '+' || c == '-' || c == '*' || c == '/' || c == '^' || c == '(' || c == ')' || c == '!' || c == '%') {
                // Handle unary minus / plus
                if (c == '-' || c == '+') {
                    boolean isUnary = false;
                    if (tokens.isEmpty()) {
                        isUnary = true;
                    } else {
                        String last = tokens.get(tokens.size() - 1);
                        if ("+-*/^(%".contains(last)) {
                            isUnary = true;
                        }
                    }
                    if (isUnary) {
                        tokens.add("0"); // e.g. -5 becomes 0 - 5
                    }
                }
                tokens.add(String.valueOf(c));
                i++;
                continue;
            }

            i++;
        }
        return tokens;
    }

    private static final Map<String, Integer> PRECEDENCE = new HashMap<>();
    static {
        PRECEDENCE.put("+", 1);
        PRECEDENCE.put("-", 1);
        PRECEDENCE.put("*", 2);
        PRECEDENCE.put("/", 2);
        PRECEDENCE.put("%", 2);
        PRECEDENCE.put("^", 3);
        PRECEDENCE.put("!", 4);
    }

    private boolean isFunction(String token) {
        switch (token) {
            case "sin":
            case "cos":
            case "tan":
            case "asin":
            case "acos":
            case "atan":
            case "log":
            case "ln":
            case "sqrt":
            case "cbrt":
            case "abs":
                return true;
            default:
                return false;
        }
    }

    private List<String> infixToPostfix(List<String> tokens) throws Exception {
        List<String> output = new ArrayList<>();
        Stack<String> stack = new Stack<>();

        for (String token : tokens) {
            if (isNumeric(token)) {
                output.add(token);
            } else if (isFunction(token)) {
                stack.push(token);
            } else if (token.equals(",")) {
                while (!stack.isEmpty() && !stack.peek().equals("(")) {
                    output.add(stack.pop());
                }
            } else if (PRECEDENCE.containsKey(token)) {
                while (!stack.isEmpty() && !stack.peek().equals("(")) {
                    String top = stack.peek();
                    if (isFunction(top) || (PRECEDENCE.containsKey(top) && PRECEDENCE.get(top) >= PRECEDENCE.get(token))) {
                        output.add(stack.pop());
                    } else {
                        break;
                    }
                }
                stack.push(token);
            } else if (token.equals("(")) {
                stack.push(token);
            } else if (token.equals(")")) {
                while (!stack.isEmpty() && !stack.peek().equals("(")) {
                    output.add(stack.pop());
                }
                if (stack.isEmpty()) {
                    throw new Exception("Mismatched parentheses");
                }
                stack.pop(); // pop '('
                if (!stack.isEmpty() && isFunction(stack.peek())) {
                    output.add(stack.pop());
                }
            }
        }

        while (!stack.isEmpty()) {
            String top = stack.pop();
            if (top.equals("(") || top.equals(")")) {
                throw new Exception("Mismatched parentheses");
            }
            output.add(top);
        }

        return output;
    }

    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private double evaluateRPN(List<String> rpn) throws Exception {
        Stack<Double> stack = new Stack<>();

        for (String token : rpn) {
            if (isNumeric(token)) {
                stack.push(Double.parseDouble(token));
            } else if (isFunction(token)) {
                if (stack.isEmpty()) throw new Exception("Invalid expression");
                double val = stack.pop();
                double res = 0.0;
                double angle = isDegree ? Math.toRadians(val) : val;

                switch (token) {
                    case "sin": res = Math.sin(angle); break;
                    case "cos": res = Math.cos(angle); break;
                    case "tan": res = Math.tan(angle); break;
                    case "asin": res = isDegree ? Math.toDegrees(Math.asin(val)) : Math.asin(val); break;
                    case "acos": res = isDegree ? Math.toDegrees(Math.acos(val)) : Math.acos(val); break;
                    case "atan": res = isDegree ? Math.toDegrees(Math.atan(val)) : Math.atan(val); break;
                    case "log": res = Math.log10(val); break;
                    case "ln": res = Math.log(val); break;
                    case "sqrt": res = Math.sqrt(val); break;
                    case "cbrt": res = Math.cbrt(val); break;
                    case "abs": res = Math.abs(val); break;
                }
                stack.push(res);
            } else if (PRECEDENCE.containsKey(token)) {
                if (token.equals("!")) {
                    if (stack.isEmpty()) throw new Exception("Invalid expression");
                    double val = stack.pop();
                    stack.push(factorial(val));
                } else {
                    if (stack.size() < 2) throw new Exception("Invalid expression");
                    double b = stack.pop();
                    double a = stack.pop();
                    double res = 0.0;
                    switch (token) {
                        case "+": res = a + b; break;
                        case "-": res = a - b; break;
                        case "*": res = a * b; break;
                        case "/":
                            if (b == 0) throw new Exception("Division by zero");
                            res = a / b;
                            break;
                        case "%": res = a * (b / 100.0); break;
                        case "^": res = Math.pow(a, b); break;
                    }
                    stack.push(res);
                }
            }
        }

        if (stack.size() != 1) {
            throw new Exception("Invalid expression");
        }
        return stack.pop();
    }

    private double factorial(double n) {
        if (n < 0 || n != Math.floor(n)) return Double.NaN;
        if (n == 0 || n == 1) return 1;
        double result = 1;
        for (int i = 2; i <= n; i++) {
            result *= i;
        }
        return result;
    }
}
