package com.example.vladimir.snake;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.util.Random;

import io.realm.Realm;

class SnakeView extends SurfaceView implements Runnable {
    // All the code will run separately to the UI
    private Thread m_Thread = null;
    // This variable determines when the game is playing
    // It is declared as volatile because
    // it can be accessed from inside and outside the thread
    private volatile boolean m_Playing;

    // This is what we draw on
    private Canvas m_Canvas;
    // This is required by the Canvas class to do the drawing
    private SurfaceHolder m_Holder;
    // This lets us control colors etc
    private Paint m_Paint;

    // This will be a reference to the Activity
    private Context m_context;

    // For tracking movement m_Direction
    public enum Direction {UP, RIGHT, DOWN, LEFT}
    // Start by heading to the right
    private Direction m_Direction = Direction.RIGHT;

    // What is the screen resolution
    private int m_ScreenWidth;
    private int m_ScreenHeight;

    // Control pausing between updates
    private long m_NextFrameTime;
    // Update the game 10 times per second
    private final long FPS = 10;
    // There are 1000 milliseconds in a second
    private final long MILLIS_IN_A_SECOND = 1000;
    // We will draw the frame much more often

    // The current Score
    private Score score;

    // Where is the mouse
    private Mouse mouse;

    // Our snake
    private Snake snake;

    // The size in pixels of a snake segment
    private int m_BlockSize;

    // The size in segments of the playable area
    private final int NUM_BLOCKS_WIDE = 40;
    private int m_NumBlocksHigh; // determined dynamically

    public SnakeView(Context context, Point size) {
        super(context);
        Realm.init(context);

        m_context = context;

        m_ScreenWidth = size.x;
        m_ScreenHeight = size.y;

        //Determine the size of each block/place on the game board
        m_BlockSize = m_ScreenWidth / NUM_BLOCKS_WIDE;
        // How many blocks of the same size will fit into the height
        m_NumBlocksHigh = m_ScreenHeight / m_BlockSize;

        // Initialize the drawing objects
        m_Holder = getHolder();
        m_Paint = new Paint();

        snake = new Snake(NUM_BLOCKS_WIDE, m_NumBlocksHigh);
        // Start the game
        startGame();
    }



    @Override
    public void run() {
        // The check for m_Playing prevents a crash at the start
        // You could also extend the code to provide a pause feature
        while (m_Playing) {
            // Update 10 times a second
            if(checkForUpdate()) {
                updateGame();
                drawGame();
            }
        }
    }

    public void pause() {
        m_Playing = false;
        try {
            m_Thread.join();
        } catch (InterruptedException e) {
            // Error
        }
    }

    public void resume() {
        m_Playing = true;
        m_Thread = new Thread(this);
        m_Thread.start();
    }

    public void startGame() {
        snake.resumeSnakeState();

        // And a mouse to eat
        spawnMouse();

        // Reset the Score
        score = new Score();

        // Setup m_NextFrameTime so an update is triggered immediately
        m_NextFrameTime = System.currentTimeMillis();
    }

    public void spawnMouse() {
        Random random = new Random();
        int m_MouseX = random.nextInt(NUM_BLOCKS_WIDE - 1) + 1;
        int m_MouseY = random.nextInt(m_NumBlocksHigh - 1) + 1;
        mouse = new Mouse(new Point(m_MouseX, m_MouseY));
        int randomMouse = random.nextInt(mouse.RANDOM_SPAWN_NUMBER) + 1;
        if (randomMouse == mouse.RANDOM_SPAWN_NUMBER) {
            mouse.generateBigMouse();
        }
    }

    private void eatMouseUpdate(){
        // Increase the size of the snake and score
        if (mouse.isBig()) {
            snake.eatBigMouse();
            score.increaseScoreValue(3);
        } else {
            snake.eatCommonMouse();
            //add to the Score
            score.increaseScoreValue(1);
        }
        //replace the mouse
        spawnMouse();
    }

    public void updateGame() {
        // Did the head of the snake touch the mouse?
        Point snakeHeadPosition = new Point(snake.getXCoordinate(0), snake.getYCoordinate(0));
        if (mouse.isBitten(snakeHeadPosition)) {
            eatMouseUpdate();
        }

        snake.move(m_Direction);
        if (snake.detectDeath()) {
            if (score.getScoreValue() > 0) {
                Realm realm = Realm.getDefaultInstance();
                realm.beginTransaction();
                Score finalScore = realm.createObject(Score.class);
                finalScore.setScoreValue(score.getScoreValue());
                realm.commitTransaction();
                realm.close();
            }
            startGame();
        }
    }

    public void drawGame() {
        // Prepare to draw
        if (m_Holder.getSurface().isValid()) {
            m_Canvas = m_Holder.lockCanvas();

            // Clear the screen with my favorite color
            m_Canvas.drawColor(Color.argb(255, 120, 87, 197));

            // Set the color of the paint to draw the snake and mouse with
            m_Paint.setColor(Color.argb(255, 255, 255, 255));
            Realm realm = Realm.getDefaultInstance();
            // Choose how big the score will be
            m_Paint.setTextSize(50);
            m_Canvas.drawText("Score: " + score.getScoreValue(), 10, 45, m_Paint);
            int highestScore = 0;
            Number dbValue = realm.where(Score.class).max("scoreValue");
            if (dbValue != null) highestScore = dbValue.intValue();
            m_Canvas.drawText("Highest score: " + highestScore, 10, 90, m_Paint);
            //Draw the snake
            realm.close();
            for (int i = 0; i < snake.getSnakeLength(); i++) {
                m_Canvas.drawRect(snake.getXCoordinate(i) * m_BlockSize,
                        (snake.getYCoordinate(i) * m_BlockSize),
                        (snake.getXCoordinate(i) * m_BlockSize) + m_BlockSize,
                        (snake.getYCoordinate(i) * m_BlockSize) + m_BlockSize,
                        m_Paint);
            }

            int[] mouseXs = mouse.getMouseXs();
            int[] mouseYs = mouse.getMouseYs();
            for (int i = 0; i < mouseXs.length; i++) {
                m_Canvas.drawRect(mouseXs[i] * m_BlockSize,
                        (mouseYs[i] * m_BlockSize),
                        (mouseXs[i] * m_BlockSize) + m_BlockSize,
                        (mouseYs[i] * m_BlockSize) + m_BlockSize,
                        m_Paint);
            }

            // Draw the whole frame
            m_Holder.unlockCanvasAndPost(m_Canvas);
        }
    }

    public boolean checkForUpdate() {

        // Are we due to update the frame
        if(m_NextFrameTime <= System.currentTimeMillis()) {
            // Tenth of a second has passed

            // Setup when the next update will be triggered
            m_NextFrameTime = System.currentTimeMillis() + MILLIS_IN_A_SECOND / FPS;

            // Return true so that the update and draw
            // functions are executed
            return true;
        }

        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {

        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                if (motionEvent.getX() >= m_ScreenWidth / 2) {
                    switch(m_Direction){
                        case UP:
                            m_Direction = Direction.RIGHT;
                            break;
                        case RIGHT:
                            m_Direction = Direction.DOWN;
                            break;
                        case DOWN:
                            m_Direction = Direction.LEFT;
                            break;
                        case LEFT:
                            m_Direction = Direction.UP;
                            break;
                    }
                } else {
                    switch(m_Direction){
                        case UP:
                            m_Direction = Direction.LEFT;
                            break;
                        case LEFT:
                            m_Direction = Direction.DOWN;
                            break;
                        case DOWN:
                            m_Direction = Direction.RIGHT;
                            break;
                        case RIGHT:
                            m_Direction = Direction.UP;
                            break;
                    }
                }
        }
        return true;
    }
}