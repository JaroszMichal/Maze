package Game;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Maze {
    private int width, height;
    private Point start;
    private Point target;
    private boolean[][][] walls;
    private boolean[][] cellVisited;
    private int margin = 10;
    private int cellSize;
    private int horizontalMargin;
    private int verticalMargin;

    public Maze(int width, int height, int screenX, int screenY) {
        this.width = width;
        this.height = height;
        start = new Point();
        target = new Point();
        this.start.x = 3;
        this.start.y = 3;
        this.target.x = width-1;
        this.target.y = height-1;
        cellVisited = new boolean[width][height];
        Point p = new Point();
        walls = new boolean[width][height][4];
        for (int i=0;i<width;i++)
            for (int j=0;j<height;j++)
                for (int k=0;k<4;k++)
                    walls[i][j][k]=true;
        cellSize = (int)Math.min((screenX-2*margin)/this.width,(screenY-2*margin)/this.height);
        horizontalMargin = (screenX - cellSize*width) / 2;
        verticalMargin = (screenY - cellSize*height) / 2;
        GenerateMaze(start);
    }


    public Point getTarget() {
        return target;
    }
    public int getHorizontalMargin() {
        return horizontalMargin;
    }
    public int getVerticalMargin() {
        return verticalMargin;
    }
    public boolean getWall(int x, int y, int dir){
        return walls[x][y][dir];
    }
    public int getMargin() {
        return margin;
    }
    public Point getStart() {
        return start;
    }
    public int getCellSize() {
        return cellSize;
    }

    private void GenerateMaze(Point point){
        cellVisited[point.x][point.y] = true;
        if (existsNotVisitedNeighbour(point)){
            int dir = selectDirection(notVisitedNeighours(point));
            removeWall(point, dir);
            GenerateMaze(nextPoint(point, dir));
        }
        if (isAtLeastOneNotVisitedCell())
            GenerateMaze(selectNewStartingPoint());
    }

    private Point selectNewStartingPoint() {
        List<Integer> list = new ArrayList<>();
        Point point = new Point();
        for (int i=0; i<this.width;i++)
            for (int j=0;j<this.height;j++) {
                point.x = i;
                point.y = j;
                if ((cellVisited[i][j])&&(existsNotVisitedNeighbour(point)))
                    list.add(j*width+i);
            }
        int randNum = ThreadLocalRandom.current().nextInt(0, list.size()); // losowy int z zakresu <0 .. x)
        point.x = list.get(randNum) % width;
        point.y = list.get(randNum) / width;
        return point;
    }

    private boolean existsNotVisitedNeighbour(Point point) {
        for (int i = 0 ; i < 4 ; i++)
            if (!isNeighbourVisited(point,i)) return true;
        return false;
    }

    private boolean isAtLeastOneNotVisitedCell() {
        for (int i=0; i<this.width;i++)
            for (int j=0;j<this.height;j++)
                if (!cellVisited[i][j]) return true;
        return false;
    }

    private Point nextPoint(Point point, int dir) {
        Point resultPoint = new Point();
        switch (dir){
            case 0:
                resultPoint.x = point.x;
                resultPoint.y = point.y - 1;
                break;
            case 1:
                resultPoint.x = point.x + 1;
                resultPoint.y = point.y;
                break;
            case 2:
                resultPoint.x = point.x;
                resultPoint.y = point.y + 1;
                break;
            case 3:
                resultPoint.x = point.x - 1;
                resultPoint.y = point.y;
                break;
        }
        return resultPoint;
    }

    private boolean[] notVisitedNeighours(Point point) {
        boolean[] result = new boolean[4];
        for (int i = 0 ; i < 4 ; i++)
            result[i]=isNeighbourVisited(point,i);
        return result;
    }

    private boolean isNeighbourVisited(Point point, int dir) {
        switch (dir){
            case 0:
                if (point.y == 0) return true;
                else return cellVisited[point.x][point.y-1];
            case 1:
                if (point.x == width-1) return true;
                else return cellVisited[point.x+1][point.y];
            case 2:
                if (point.y == height-1) return true;
                else return cellVisited[point.x][point.y+1];
            case 3:
                if (point.x == 0) return true;
                else return cellVisited[point.x-1][point.y];
            default:
                return true;
        }
    }
    private int numberNotVisitedNeighbours(boolean[] nvn){
        int result = 0;
        for (int i = 0 ; i < nvn.length ; i++)
            if (!nvn[i]) result++;
        return result;
    }

    private int selectDirection( boolean[] nvn){
        int randNum = ThreadLocalRandom.current().nextInt(0, numberNotVisitedNeighbours(nvn)); // losowy int z zakresu <0 .. x)
        int tmp = 0;
        for (int i = 0 ; i < nvn.length ; i++){
            if (!nvn[i])
                if (tmp == randNum) return i;
                else tmp++;
        }
        return -1;
    }

    private void removeWall(Point point, int dir) {
        walls[point.x][point.y][dir] = false;
        int xtmp = point.x;
        int ytmp = point.y;
        switch (dir){
            case 0:
                ytmp--;
                break;
            case 1:
                xtmp++;
                break;
            case 2:
                ytmp++;
                break;
            case 3:
                xtmp--;
                break;
        }
        if ((xtmp>=0)&&(xtmp<this.width)&&(ytmp>=0)&&(ytmp<this.height))
            walls[xtmp][ytmp][(dir+2)%4]=false;

    }

    public void drawMaze(Canvas canvas, int screenWidth, int screenHeight, Paint paint){
        paint.setStrokeWidth(cellSize / 20);
        paint.setColor(Color.WHITE);
        for (int i = 0;i<width;i++)
            for (int j=0;j<height;j++){
                if (walls[i][j][0])
                    canvas.drawLine(i*cellSize+horizontalMargin,j*cellSize+verticalMargin,(i+1)*cellSize+horizontalMargin, j*cellSize+verticalMargin, paint);
                if (walls[i][j][1])
                    canvas.drawLine((i+1)*cellSize+horizontalMargin,j*cellSize+verticalMargin,(i+1)*cellSize+horizontalMargin, (j+1)*cellSize+verticalMargin, paint);
                if (walls[i][j][2])
                    canvas.drawLine(i*cellSize+horizontalMargin,(j+1)*cellSize+verticalMargin,(i+1)*cellSize+horizontalMargin, (j+1)*cellSize+verticalMargin, paint);
                if (walls[i][j][3])
                    canvas.drawLine(i*cellSize+horizontalMargin,j*cellSize+verticalMargin,i*cellSize+horizontalMargin, (j+1)*cellSize+verticalMargin, paint);
            }
        paint.setColor(Color.GREEN);
        canvas.drawCircle((int)((target.x+0.5)*cellSize+horizontalMargin), (int)((target.y+0.5)*cellSize+verticalMargin), (int)(cellSize/4), paint);
    }

    public Point getStartBallPosition(float proportion){
        Point point = new Point();
        point.x = (int)((start.x+(1-proportion)/2)*cellSize+horizontalMargin);
        point.y = (int)((start.y+(1-proportion)/2)*cellSize+verticalMargin);
        return point;
    }

    public Point getTargetBallCenterPosition(){
        Point point = new Point();
        point.x = (int)((target.x+0.5)*cellSize+horizontalMargin);
        point.y = (int)((target.y+0.5)*cellSize+verticalMargin);
        return point;
    }


}
