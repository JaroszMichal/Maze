package Game;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.example.labirynt2.R;

import static Game.GameView.screenRatioX;
import static Game.GameView.screenRatioY;

public class Ball {
    Bitmap ballBitmap;

    Ball (int size, Resources res){
        ballBitmap = BitmapFactory.decodeResource(res, R.drawable.sphere);
        ballBitmap = Bitmap.createScaledBitmap(ballBitmap,size, size,false);
    }
}
