package com.example.snakegame2d;

import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    private final List<SnakePoints> snakePointsList = new ArrayList<>();
    private SurfaceView surfaceView;
    private TextView scoreTV;
    private SurfaceHolder surfaceHolder;
    private String movingPosition = "right";
    private int score = 0;
    private static final int pointSize = 28;
    private static final int defaultSize = 3;
    private static final int snakeColor = Color.BLUE;
    private static final int snakeMovingSpeed = 800;
    private int positionX, positionY;
    private Timer timer;
    private Canvas canvas = null;
    private Paint pointColor = null;

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

        surfaceView = findViewById(R.id.surfaceView);
        scoreTV = findViewById(R.id.scoreTV);
        final AppCompatImageButton topBtn = findViewById(R.id.topBtn);
        final AppCompatImageButton leftBtn = findViewById(R.id.leftBtn);
        final AppCompatImageButton rightBtn = findViewById(R.id.rightBtn);
        final AppCompatImageButton downBtn = findViewById(R.id.downBtn);

        surfaceView.getHolder().addCallback(this);

        topBtn.setOnClickListener(v -> {
            if (!movingPosition.equals("bottom")) movingPosition = "top";
        });
        leftBtn.setOnClickListener(v -> {
            if (!movingPosition.equals("right")) movingPosition = "left";
        });
        rightBtn.setOnClickListener(v -> {
            if (!movingPosition.equals("left")) movingPosition = "right";
        });
        downBtn.setOnClickListener(v -> {
            if (!movingPosition.equals("top")) movingPosition = "bottom";
        });
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        this.surfaceHolder = holder;
        init();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {}

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {}

    private void init() {
        snakePointsList.clear();
        scoreTV.setText("0");
        score = 0;
        movingPosition = "right";

        int startPositionX = pointSize * defaultSize;
        for (int i = 0; i < defaultSize; i++) {
            SnakePoints snakePoints = new SnakePoints(startPositionX, pointSize);
            snakePointsList.add(snakePoints);
            startPositionX = startPositionX - (pointSize * 2);
        }

        addPoint();
        moveSnake();
    }

    private void addPoint() {
        int surfaceWidth = surfaceView.getWidth() - (pointSize * 2);
        int surfaceHeight = surfaceView.getHeight() - (pointSize * 2);

        do {
            int randomXPosition = new Random().nextInt(surfaceWidth / pointSize);
            int randomYPosition = new Random().nextInt(surfaceHeight / pointSize);

            if ((randomXPosition % 2) != 0) randomXPosition += 1;
            if ((randomYPosition % 2) != 0) randomYPosition += 1;

            positionX = (pointSize * randomXPosition) + pointSize;
            positionY = (pointSize * randomYPosition) + pointSize;
        } while (isFoodOnSnake(positionX, positionY));
    }

    private boolean isFoodOnSnake(int x, int y) {
        for (SnakePoints point : snakePointsList) {
            if (point.getPositionX() == x && point.getPositionY() == y) {
                return true;
            }
        }
        return false;
    }

    private void moveSnake() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                int headPositionX = snakePointsList.get(0).getPositionX();
                int headPositionY = snakePointsList.get(0).getPositionY();

                switch (movingPosition) {
                    case "right":
                        headPositionX += (pointSize * 2);
                        break;
                    case "left":
                        headPositionX -= (pointSize * 2);
                        break;
                    case "top":
                        headPositionY -= (pointSize * 2);
                        break;
                    case "bottom":
                        headPositionY += (pointSize * 2);
                        break;
                }

                if (headPositionX == positionX && headPositionY == positionY) {
                    growSnake();
                    addPoint();
                }
//each segment moves to the position of the segment in front of it.
                for (int i = snakePointsList.size() - 1; i > 0; i--) {
                    snakePointsList.get(i).setPositionX(snakePointsList.get(i - 1).getPositionX());
                    snakePointsList.get(i).setPositionY(snakePointsList.get(i - 1).getPositionY());
                }

                snakePointsList.get(0).setPositionX(headPositionX);
                snakePointsList.get(0).setPositionY(headPositionY);

                if (checkGameOver(headPositionX, headPositionY)) {
                    timer.purge();
                    timer.cancel();
                    runOnUiThread(() -> showGameOverDialog());
                } else {
                    drawSnake();
                }
            }
        }, 1000 - snakeMovingSpeed, 1000 - snakeMovingSpeed);
    }

    private void growSnake() {
        SnakePoints snakePoints = new SnakePoints(0, 0);
        snakePointsList.add(snakePoints);
        score++;
        runOnUiThread(() -> scoreTV.setText(String.valueOf(score)));
    }

    private boolean checkGameOver(int headPositionX, int headPositionY) {
        boolean gameOver = false;
        if (snakePointsList.get(0).getPositionX() < 0 || snakePointsList.get(0).getPositionY() < 0 ||
                snakePointsList.get(0).getPositionX() >= surfaceView.getWidth() ||
                snakePointsList.get(0).getPositionY() >= surfaceView.getHeight()) {
            gameOver = true;
        } else {
            for (int i = 1; i < snakePointsList.size(); i++) {
                if (headPositionX == snakePointsList.get(i).getPositionX() &&
                        headPositionY == snakePointsList.get(i).getPositionY()) {
                    gameOver = true;
                    break;
                }
            }
        }
        return gameOver;
    }

    private void showGameOverDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Your Score: " + score);
        builder.setTitle("Game Over");
        builder.setCancelable(false);
        builder.setPositiveButton("Restart", (dialog, which) -> init());
        builder.show();
    }

    private void drawSnake() {
        canvas = surfaceHolder.lockCanvas();
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.CLEAR);

        canvas.drawCircle(positionX, positionY, pointSize, createPointColor());

        for (SnakePoints point : snakePointsList) {
            canvas.drawCircle(point.getPositionX(), point.getPositionY(), pointSize, createPointColor());
        }

        surfaceHolder.unlockCanvasAndPost(canvas);
    }

    private Paint createPointColor() {
        if (pointColor == null) {
            pointColor = new Paint();
            pointColor.setColor(snakeColor);
            pointColor.setStyle(Paint.Style.FILL);
            pointColor.setAntiAlias(true);
        }
        return pointColor;
    }
}
