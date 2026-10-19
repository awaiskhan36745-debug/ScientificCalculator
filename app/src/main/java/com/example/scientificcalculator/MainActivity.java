package com.example.scientificcalculator;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView tvExpression, tvResult;
    private MaterialButton btnDegRad;
    private ImageView btnHistory;

    private final StringBuilder currentExpression = new StringBuilder();
    private String lastAns = "0";
    private boolean isDegree = true;
    private final ExpressionEvaluator evaluator = new ExpressionEvaluator();
    private final List<String> historyList = new ArrayList<>();

    private final DecimalFormat decimalFormat = new DecimalFormat("#.##########");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        tvExpression = findViewById(R.id.tvExpression);
        tvResult = findViewById(R.id.tvResult);
        btnDegRad = findViewById(R.id.btnDegRad);
        btnHistory = findViewById(R.id.btnHistory);
    }

    private void setupClickListeners() {
        // Number buttons
        int[] numberIds = {
                R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9, R.id.btnDot
        };

        View.OnClickListener numberListener = v -> {
            Button b = (Button) v;
            appendExpression(b.getText().toString());
        };

        for (int id : numberIds) {
            findViewById(id).setOnClickListener(numberListener);
        }

        // Operator buttons
        findViewById(R.id.btnPlus).setOnClickListener(v -> appendExpression("+"));
        findViewById(R.id.btnMinus).setOnClickListener(v -> appendExpression("-"));
        findViewById(R.id.btnMultiply).setOnClickListener(v -> appendExpression("×"));
        findViewById(R.id.btnDivide).setOnClickListener(v -> appendExpression("÷"));
        findViewById(R.id.btnPower).setOnClickListener(v -> appendExpression("^"));
        findViewById(R.id.btnPercent).setOnClickListener(v -> appendExpression("%"));

        // Scientific functions
        findViewById(R.id.btnSin).setOnClickListener(v -> appendFunction("sin("));
        findViewById(R.id.btnCos).setOnClickListener(v -> appendFunction("cos("));
        findViewById(R.id.btnTan).setOnClickListener(v -> appendFunction("tan("));
        findViewById(R.id.btnLog).setOnClickListener(v -> appendFunction("log("));
        findViewById(R.id.btnLn).setOnClickListener(v -> appendFunction("ln("));
        findViewById(R.id.btnSqrt).setOnClickListener(v -> appendFunction("sqrt("));
        findViewById(R.id.btnAbs).setOnClickListener(v -> appendFunction("abs("));

        findViewById(R.id.btnPi).setOnClickListener(v -> appendExpression("π"));
        findViewById(R.id.btnE).setOnClickListener(v -> appendExpression("e"));
        findViewById(R.id.btnFact).setOnClickListener(v -> appendExpression("!"));
        findViewById(R.id.btnOpenBracket).setOnClickListener(v -> appendExpression("("));
        findViewById(R.id.btnCloseBracket).setOnClickListener(v -> appendExpression(")"));
        findViewById(R.id.btnAns).setOnClickListener(v -> appendExpression(lastAns));

        // Actions
        findViewById(R.id.btnAc).setOnClickListener(v -> clearAll());
        findViewById(R.id.btnDel).setOnClickListener(v -> deleteLast());
        findViewById(R.id.btnEquals).setOnClickListener(v -> calculateResult());

        // Mode and History
        btnDegRad.setOnClickListener(v -> {
            isDegree = !isDegree;
            btnDegRad.setText(isDegree ? "DEG" : "RAD");
            evaluator.setDegree(isDegree);
        });

        btnHistory.setOnClickListener(v -> showHistoryDialog());
    }

    private void appendExpression(String str) {
        currentExpression.append(str);
        tvExpression.setText(currentExpression.toString());
        tryCalculatePreview();
    }

    private void appendFunction(String func) {
        currentExpression.append(func);
        tvExpression.setText(currentExpression.toString());
    }

    private void clearAll() {
        currentExpression.setLength(0);
        tvExpression.setText("");
        tvResult.setText("0");
    }

    private void deleteLast() {
        int len = currentExpression.length();
        if (len > 0) {
            currentExpression.deleteCharAt(len - 1);
            tvExpression.setText(currentExpression.toString());
            tryCalculatePreview();
        }
    }

    private void tryCalculatePreview() {
        String expr = currentExpression.toString();
        if (expr.isEmpty()) {
            tvResult.setText("0");
            return;
        }
        try {
            double res = evaluator.evaluate(expr);
            if (!Double.isNaN(res) && !Double.isInfinite(res)) {
                tvResult.setText(decimalFormat.format(res));
            }
        } catch (Exception ignored) {
            // Preview might fail on incomplete syntax (e.g. "sin("), which is expected
        }
    }

    private void calculateResult() {
        String expr = currentExpression.toString();
        if (expr.isEmpty()) return;

        try {
            double res = evaluator.evaluate(expr);
            if (Double.isNaN(res) || Double.isInfinite(res)) {
                tvResult.setText("Error");
                return;
            }
            String formattedResult = decimalFormat.format(res);
            tvResult.setText(formattedResult);

            // Save to history
            String historyItem = expr + " = " + formattedResult;
            historyList.add(0, historyItem);

            lastAns = formattedResult;
            currentExpression.setLength(0);
            currentExpression.append(formattedResult);
            tvExpression.setText("");
        } catch (Exception e) {
            tvResult.setText("Error");
            Toast.makeText(this, "Invalid Expression: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showHistoryDialog() {
        if (historyList.isEmpty()) {
            Toast.makeText(this, "No history available", Toast.LENGTH_SHORT).show();
            return;
        }

        CharSequence[] items = historyList.toArray(new CharSequence[0]);
        new AlertDialog.Builder(this, com.google.android.material.R.style.Theme_Material3_DayNight_Dialog)
                .setTitle("Calculation History")
                .setItems(items, (dialog, which) -> {
                    String selected = historyList.get(which);
                    // Extract result part after "="
                    String[] parts = selected.split("=");
                    if (parts.length > 1) {
                        String resVal = parts[1].trim();
                        currentExpression.append(resVal);
                        tvExpression.setText(currentExpression.toString());
                        tryCalculatePreview();
                    }
                })
                .setPositiveButton("Clear History", (dialog, which) -> historyList.clear())
                .setNegativeButton("Close", null)
                .show();
    }
}
