package com.rgw3d.multiwiicontroller;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

/**
 * Code for a tri state toggle button
 * Created by rgw3d on 6/20/2016.
 */
public class TriToggleButton extends Button {
    private static final int[] STATE_LOW = { R.attr.low };

    private static final int[] STATE_MID = { R.attr.mid };

    private static final int[] STATE_HIGH = { R.attr.high};

    private int _currState = 1;

    public TriToggleButton(Context context) {
        super(context);
        setText();
    }

    public TriToggleButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setText();
    }

    public TriToggleButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setText();
    }


    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        // Add the number of states you have
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 3);

        if(_currState == 0) {
            mergeDrawableStates(drawableState, STATE_LOW);
        }
        else if(_currState == 1) {
            mergeDrawableStates(drawableState, STATE_MID);
        }
        else {
            mergeDrawableStates(drawableState, STATE_HIGH);
        }

        return drawableState;
    }

    @Override
    public boolean performClick() {
        nextState();
        setText();
        return super.performClick();
    }

    public int getValue(){
        switch(_currState) {
            case 0:
                return 1000;
            case 1:
                return 1500;
            case 2:
                return 2000;
            default:
                return 1501;
        }

    }

    public void nextState(){
        _currState = (_currState+1)%3;
    }
    public void setText(){
        switch(_currState) {
            case 0: this.setText("1000");
                break;
            case 1: this.setText("1500");
                break;
            case 2: this.setText("2000");
                break;
            default: this.setText(":(");
                break;
        }
    }
}
