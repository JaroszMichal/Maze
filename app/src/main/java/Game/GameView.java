package Game;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.MotionEvent;
import android.view.SurfaceView;

public class GameView extends SurfaceView implements Runnable, SensorEventListener {

    private Thread thread;
    private boolean isPlaying;
    private int screenX, screenY;
    public static float screenRatioX, screenRatioY;
    private Paint paint;
    private Ball ball;
    boolean[] ballMoveDirection;
    int radius;
    private Point currentBallPosition;
    private float proportion;
    private Background background1, background2;
    boolean finalCountDown, isOver;
    Maze maze;
    private SensorManager sensorManager;
    private Sensor accelerometr;
    private float accX, accY, accZ;
    private boolean accSteering;
    private long gameTime, startTime;
    private boolean inTheDarkness;
    private SharedPreferences prefs;
    private GameActivity activity;

    public GameView(GameActivity activity, int screenX, int screenY){//, boolean inTheDarkness) {
        super(activity);
        this.activity = activity;
        prefs = activity.getSharedPreferences("game", Context.MODE_PRIVATE);
        this.screenX = screenX;
        this.screenY = screenY;
        isPlaying = true;
        screenRatioX = 1920f / screenX;
        screenRatioY = 1080f / screenY;
        background1 = new Background(screenX, screenX, getResources());
        background2 = new Background(screenX, screenX, getResources());
        background2.x = screenX;
        String lv=prefs.getString("level", "Łatwy");
        int w=10;
        int h=5;
        if (lv.equals("Łatwy")){
            w = 10;
            h = 5;
        }
        if (lv.equals("Średni")){
            w = 20;
            h = 10;
        }
        if (lv.equals("Trudny")){
            w = 40;
            h = 20;
        }
        if (lv.equals("Bardzo trudny")){
            w = 60;
            h = 30;
        }
        maze = new Maze(w,h, screenX, screenY);
        lv=prefs.getString("steering", "Akcelerometr");
        if (lv.equals("Akcelerometr"))
            this.accSteering = true;
        else
            this.accSteering = false;
        proportion = (float)2.0/3;
        ball = new Ball((int)(maze.getCellSize()*proportion) ,getResources());
        currentBallPosition = new Point();
        currentBallPosition.x = maze.getStartBallPosition(proportion).x;
        currentBallPosition.y = maze.getStartBallPosition(proportion).y;
        radius = (int)((maze.getCellSize()*proportion)/2);
        finalCountDown = false;
        isOver = false;
        paint = new Paint();
        ballMoveDirection = new boolean[4];
        sensorManager = (SensorManager) activity.getSystemService(activity.SENSOR_SERVICE);
        accelerometr = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometr, SensorManager.SENSOR_DELAY_NORMAL);
        accX=0;
        accY=0;
        accZ=0;
        this.inTheDarkness = inTheDarkness;
        gameTime=0;
    }

    @Override
    public void run() {
        startTime = System.currentTimeMillis();
        while (isPlaying){
            update();
            draw();
            sleep();
        }
    }

    private void update(){
        int distanceToMove;
        Point tryToMove = new Point();
        int x,y;
        int posX = currentBallPosition.x;
        int posY = currentBallPosition.y;
        int znakX, znakY;
        if (!finalCountDown)
            if (accSteering){
                distanceToMove = 80;
                y = (int)(distanceToMove*accX/10);
                x = (int)(distanceToMove*accY/10);
                if (x>=0) znakX = 1;
                else{
                    znakX = -1;
                    x = -x;
                }
                if (y>=0) znakY = 1; else {
                    znakY = -1;
                    y = -y;
                }
                int tmpX, tmpY;
                for (int i=0;i<Math.max(x,y);i++){
                    tmpX = posX;
                    tmpY = posY;
                    if (i<x) {
                        tmpX += znakX;
                        tryToMove.x = tmpX;
                        tryToMove.y = tmpY;
                        if (!isColision(tryToMove)) posX = tmpX;
                    }
                    if (i<y) {
                        tmpY += znakY;
                        tryToMove.x = tmpX;
                        tryToMove.y = tmpY;
                        if (!isColision(tryToMove)) posY = tmpY;
                    }
                }
                currentBallPosition.x = posX;
                currentBallPosition.y = posY;
            }
            else
                if (ballMoveDirection[0]||ballMoveDirection[1]||ballMoveDirection[2]||ballMoveDirection[3]) {
                    distanceToMove = 5;
                    x = 0;
                    y = 0;
                    if (ballMoveDirection[0]) y = -distanceToMove;
                    if (ballMoveDirection[1]) x = distanceToMove;
                    if (ballMoveDirection[2]) y = distanceToMove;
                    if (ballMoveDirection[3]) x = -distanceToMove;
                    if (x>=0) znakX = 1;
                    else{
                        znakX = -1;
                        x = -x;
                    }
                    if (y>=0) znakY = 1; else {
                        znakY = -1;
                        y = -y;
                    }
                    int tmpX, tmpY;
                    for (int i=0;i<Math.max(x,y);i++){
                        tmpX = posX;
                        tmpY = posY;
                        if (i<x) {
                            tmpX += znakX;
                            tryToMove.x = tmpX;
                            tryToMove.y = tmpY;
                            if (!isColision(tryToMove)) posX = tmpX;
                        }
                        if (i<y) {
                            tmpY += znakY;
                            tryToMove.x = tmpX;
                            tryToMove.y = tmpY;
                            if (!isColision(tryToMove)) posY = tmpY;
                        }
                    }
                    currentBallPosition.x = posX;
                    currentBallPosition.y = posY;
                }else{}
        else {
            boolean xInTarget = false;
            boolean yInTarget = false;
            if (currentBallPosition.x +radius > maze.getTargetBallCenterPosition().x) currentBallPosition.x--;
            else
                if (currentBallPosition.x +radius < maze.getTargetBallCenterPosition().x) currentBallPosition.x++;
                else
                    xInTarget = true;
            if (currentBallPosition.y +radius > maze.getTargetBallCenterPosition().y) currentBallPosition.y--;
            else
                if (currentBallPosition.y +radius < maze.getTargetBallCenterPosition().y) currentBallPosition.y++;
                else
                    yInTarget = true;
                isOver = xInTarget && yInTarget;
        }
    }

    private void draw(){
        if (getHolder().getSurface().isValid()){
            Canvas canvas = getHolder().lockCanvas();
            canvas.drawBitmap(background1.background, background1.x, background1.y, paint);
            maze.drawMaze(canvas,screenX,screenY,paint);
            canvas.drawBitmap(ball.ballBitmap,currentBallPosition.x , currentBallPosition.y, paint);

            paint.setColor(Color.LTGRAY);
            paint.setTextSize(60);
            paint.setTypeface(Typeface.create("Arial",Typeface.ITALIC));
            paint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(timeFormat(gameTime+System.currentTimeMillis()-startTime),((currentBallPosition.x>canvas.getWidth()*2/3)&&(currentBallPosition.y<300))? (canvas.getWidth()/2):(canvas.getWidth()-50), 100,paint);
            if (!finalCountDown)
                if (isGameOver())
                    finalCountDown = true;
            if (isOver){
                isPlaying = false;
                gameTime+=System.currentTimeMillis()-startTime;
                getHolder().unlockCanvasAndPost(canvas);
                saveifHighScore();
                activity.startActivity(new Intent(activity, WhatNextActivity.class));
                return;
            }
            getHolder().unlockCanvasAndPost(canvas);
        }
    }

    private void saveifHighScore() {
        if (prefs.getLong("highscore", 0)>gameTime){
            SharedPreferences.Editor editor = prefs.edit();
            editor.putLong("highscore", gameTime);
            editor.apply();
        }
    }

    private String timeFormat(long t) {
        long h, m, s, ms;
        ms = t % 1000;
        s = ((t-ms) / 1000) % 60;
        m = (((t-ms) / 1000) / 60) % 60;
        h =  (((t-ms) / 1000) / 60) / 60;
        return fillWithZeros(h,1,true,":")+fillWithZeros(m,2, true,":")+fillWithZeros(s,2,false,":")+fillWithZeros(ms,3,false,"");
    }

    private String fillWithZeros(long s, int i, boolean dontShowIfZero, String add) {
        String result="";
        if ((dontShowIfZero) && (s == 0)) return "";
        result = String.valueOf(s);
        while (result.length()<i)
            result = "0"+result;
        return result+add;
    }

    private boolean isGameOver() {
        Point target = new Point();
        target = maze.getTargetBallCenterPosition();
        if(distance(currentBallPosition.x+radius, currentBallPosition.y+radius, target)<(radius))
            return true;
        else
            return false;
    }

    private boolean isColision(Point ballPosition) {
        Point ballCenter = new Point();
        ballCenter.x = ballPosition.x+radius;
        ballCenter.y = ballPosition.y+radius;
        Point inCell = new Point();

        inCell.x = (ballCenter.x - maze.getHorizontalMargin()) / maze.getCellSize();
        inCell.y = (ballCenter.y - maze.getVerticalMargin()) / maze.getCellSize();

        int leftX = inCell.x*maze.getCellSize()+maze.getHorizontalMargin();
        int rightX =(inCell.x+1)*maze.getCellSize()+maze.getHorizontalMargin();
        int topY = inCell.y*maze.getCellSize()+maze.getVerticalMargin();
        int bottomY =(inCell.y+1)*maze.getCellSize()+maze.getVerticalMargin();

        if ((maze.getWall(inCell.x, inCell.y, 0))&&(ballCenter.y<topY+radius)) return true;
        if ((maze.getWall(inCell.x, inCell.y, 1))&&(ballCenter.x>rightX-radius)) return true;
        if ((maze.getWall(inCell.x, inCell.y, 2))&&(ballCenter.y>bottomY-radius)) return true;
        if ((maze.getWall(inCell.x, inCell.y, 3))&&(ballCenter.x<leftX+radius)) return true;
        if ((distance(leftX,topY,ballCenter)<radius)
                &&(!maze.getWall(inCell.x, inCell.y,3))
                &&(!maze.getWall(inCell.x, inCell.y,0))
                &&((maze.getWall(inCell.x-1, inCell.y,0))
                    ||(maze.getWall(inCell.x, inCell.y-1,3)))) return true;
        if ((distance(rightX,topY,ballCenter)<radius)
                &&(!maze.getWall(inCell.x, inCell.y,1))
                &&(!maze.getWall(inCell.x, inCell.y,0))
                &&((maze.getWall(inCell.x+1, inCell.y,0))
                    ||(maze.getWall(inCell.x, inCell.y-1,1)))) return true;
        if ((distance(rightX,bottomY,ballCenter)<radius)
                &&(!maze.getWall(inCell.x, inCell.y,1))
                &&(!maze.getWall(inCell.x, inCell.y,2))
                &&((maze.getWall(inCell.x+1, inCell.y,2))
                    ||(maze.getWall(inCell.x, inCell.y+1,1)))) return true;
        if ((distance(leftX,bottomY,ballCenter)<radius)
                &&(!maze.getWall(inCell.x, inCell.y,3))
                &&(!maze.getWall(inCell.x, inCell.y,2))
                &&((maze.getWall(inCell.x-1, inCell.y,2))
                    ||(maze.getWall(inCell.x, inCell.y+1,3)))) return true;
        return false;
    }

    private float distance(int x, int y, Point ballCenter) {
        return (int)Math.sqrt(Math.pow(ballCenter.x-x,2)+Math.pow(ballCenter.y-y,2));
    }

    private void sleep(){
        try {
            Thread.sleep(17);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void resume(){
        startTime=System.currentTimeMillis();
        isPlaying = true;
        thread = new Thread(this);
        thread.start();
    }

    public void pause(){
        try {
            gameTime+=System.currentTimeMillis()-startTime;
            isPlaying = false;
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                if ((event.getX()<screenX / 3)&&(event.getY()<screenY / 3)){
                    ballMoveDirection[0] = true;
                    ballMoveDirection[1] = false;
                    ballMoveDirection[2] = false;
                    ballMoveDirection[3] = true;
                }else
                if ((event.getX()>=screenX / 3)&&(event.getX()<2*screenX / 3)&&(event.getY()<screenY / 3)){
                    ballMoveDirection[0] = true;
                    ballMoveDirection[1] = false;
                    ballMoveDirection[2] = false;
                    ballMoveDirection[3] = false;
                }else
                if ((event.getX()>=2*screenX / 3)&&(event.getY()<screenY / 3)){
                    ballMoveDirection[0] = true;
                    ballMoveDirection[1] = true;
                    ballMoveDirection[2] = false;
                    ballMoveDirection[3] = false;
                }else
                if ((event.getX()<screenX / 3)&&(event.getY()>=screenY / 3)&&(event.getY()<2*screenY / 3)){
                    ballMoveDirection[0] = false;
                    ballMoveDirection[1] = false;
                    ballMoveDirection[2] = false;
                    ballMoveDirection[3] = true;
                }else
                if ((event.getX()>=2*screenX / 3)&&(event.getY()>=screenY / 3)&&(event.getY()<2*screenY / 3)){
                    ballMoveDirection[0] = false;
                    ballMoveDirection[1] = true;
                    ballMoveDirection[2] = false;
                    ballMoveDirection[3] = false;
                }else
                if ((event.getX()<screenX / 3)&&(event.getY()>=2*screenY / 3)){
                    ballMoveDirection[0] = false;
                    ballMoveDirection[1] = false;
                    ballMoveDirection[2] = true;
                    ballMoveDirection[3] = true;
                }else
                if ((event.getX()>=screenX / 3)&&(event.getX()<2*screenX / 3)&&(event.getY()>=2*screenY / 3)){
                    ballMoveDirection[0] = false;
                    ballMoveDirection[1] = false;
                    ballMoveDirection[2] = true;
                    ballMoveDirection[3] = false;
                }else
                if ((event.getX()>=2*screenX / 3)&&(event.getY()>=2*screenY / 3)){
                    ballMoveDirection[0] = false;
                    ballMoveDirection[1] = true;
                    ballMoveDirection[2] = true;
                    ballMoveDirection[3] = false;
                }
                break;
            case MotionEvent.ACTION_UP:
                    ballMoveDirection[0] = false;
                    ballMoveDirection[1] = false;
                    ballMoveDirection[2] = false;
                    ballMoveDirection[3] = false;
                break;
        }

        return true;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        accX = sensorEvent.values[0];
        accY = sensorEvent.values[1];
        accZ = sensorEvent.values[2];

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
