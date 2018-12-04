package com.example.vladimir.snake;

class Snake {
    private int[] x;
    private int[] y;
    private int snakeLength;
    private int numBlocksWidth;
    private int numBlocksHeight;

    public Snake(int numBlocksWidth, int numBlocksHeight) {
        this.x = new int[200];
        this.y = new int[200];
        this.numBlocksWidth = numBlocksWidth;
        this.numBlocksHeight = numBlocksHeight;
    }

    public int getSnakeLength() {
        return this.snakeLength;
    }

    public void setSnakeLength(int snakeLength) {
        this.snakeLength = snakeLength;
    }

    public void insertIntoSnakeX(int i, int positionX) {
        this.x[i] = positionX;
    }

    public void insertIntoSnakeY(int i, int positionY) {
        this.y[i] = positionY;
    }

    public void eatBigMouse() {
        this.snakeLength += 3;
    }

    public void eatCommonMouse() {
        this.snakeLength++;
    }

    public void move(SnakeView.Direction direction) {
        for (int i = this.snakeLength; i > 0; i--) {
            // Start at the back and move it
            // to the position of the segment in front of it
            this.x[i] = this.x[i - 1];
            this.y[i] = this.y[i - 1];

            // Exclude the head because
            // the head has nothing in front of it
        }

        // Move the head in the appropriate m_Direction
        switch (direction) {
            case UP:
                this.y[0]--;
                break;

            case RIGHT:
                this.x[0]++;
                break;

            case DOWN:
                this.y[0]++;
                break;

            case LEFT:
                this.x[0]--;
                break;
        }
    }

    public boolean detectDeath() {
        // Has the snake died?
        boolean dead = false;

        // Hit a wall?
        if (this.x[0] == -1) dead = true;
        if (this.x[0] >= this.numBlocksWidth) dead = true;
        if (this.y[0] == -1) dead = true;
        if (this.y[0] == this.numBlocksHeight) dead = true;

        // Eaten itself?
        for (int i = this.snakeLength - 1; i > 0; i--) {
            if ((i > 4) && (this.x[0] == this.x[i]) && (this.y[0] == this.y[i])) {
                dead = true;
            }
        }
        return dead;
    }

    public void resumeSnakeState() {
        setSnakeLength(1);
        insertIntoSnakeX(0, this.numBlocksWidth / 2);
        insertIntoSnakeY(0, this.numBlocksHeight / 2);
    }

    public int getXCoordinate(int i) {
        return this.x[i];
    }

    public int getYCoordinate(int i) {
        return this.y[i];
    }
}
