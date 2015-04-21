package sg.edu.nus.photopix;

/**
 * Created by linxiuqing on 7/4/15.
 */
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;


public class GestureDetector {
    private static final String TAG = "GestureDetector";

    public interface OnGestureListener {

        public boolean onScale(View view, GestureDetector detector);

        public boolean onScaleBegin(View view, GestureDetector detector);

        public void onScaleEnd(View view, GestureDetector detector);
    }

    public static class SimpleOnGestureListener implements OnGestureListener {

        public boolean onScale(View view, GestureDetector detector) {
            return false;
        }

        public boolean onScaleBegin(View view, GestureDetector detector) {
            return true;
        }

        public void onScaleEnd(View view, GestureDetector detector) { }
    }

    private static final float PRESSURE_THRESHOLD = 0.67f;

    private final OnGestureListener pListener;
    private boolean pGestureInProgress;

    private MotionEvent pEvent;
    private MotionEvent cEvent;

    private Vector2D currSpanVector;
    private float focusX;
    private float focusY;
    private float pFingerDiffX;
    private float pFingerDiffY;
    private float cFingerDiffX;
    private float cFingerDiffY;
    private float cLen;
    private float pLen;
    private float scaleFactor;
    private float cPressure;
    private float pPressure;
    private long mTimeDelta;

    private boolean invalidGesture;

    // Pointer IDs currently responsible for the two fingers controlling the gesture
    private int activeId0;
    private int activeId1;
    private boolean activeMostRecent;

    public GestureDetector(OnGestureListener listener) {
        pListener = listener;
        currSpanVector = new Vector2D();
    }

    public boolean onTouchEvent(View view, MotionEvent event) {
        final int action = event.getActionMasked();

        if (action == MotionEvent.ACTION_DOWN) {
            reset();
        }

        boolean handle = true;
        if (invalidGesture) {
            handle = false;
        } else if (!pGestureInProgress) {
            switch (action) {
                case MotionEvent.ACTION_DOWN: {
                    activeId0 = event.getPointerId(0);
                    activeMostRecent = true;
                }
                break;

                case MotionEvent.ACTION_UP:
                    reset();
                    break;

                case MotionEvent.ACTION_POINTER_DOWN: {
                    // a new multi-finger gesture
                    if (pEvent != null) pEvent.recycle();
                    pEvent = MotionEvent.obtain(event);
                    mTimeDelta = 0;

                    int index1 = event.getActionIndex();
                    int index0 = event.findPointerIndex(activeId0);
                    activeId1 = event.getPointerId(index1);
                    if (index0 < 0 || index0 == index1) {
                        // for a broken event stream.
                        index0 = findNewActiveIndex(event, activeId1, -1);
                        activeId0 = event.getPointerId(index0);
                    }
                    activeMostRecent = false;

                    setContext(view, event);

                    pGestureInProgress = pListener.onScaleBegin(view, this);
                    break;
                }
            }
        } else {
            // Transform gesture in progress - attempt to handle it
            switch (action) {
                case MotionEvent.ACTION_POINTER_DOWN: {
                    // End the old gesture and begin a new one with the most recent two fingers.
                    pListener.onScaleEnd(view, this);
                    final int oldActive0 = activeId0;
                    final int oldActive1 = activeId1;
                    reset();

                    pEvent = MotionEvent.obtain(event);
                    activeId0 = activeMostRecent ? oldActive0 : oldActive1;
                    activeId1 = event.getPointerId(event.getActionIndex());
                    activeMostRecent = false;

                    int index0 = event.findPointerIndex(activeId0);
                    if (index0 < 0 || activeId0 == activeId1) {
                        // for a broken event stream.
                        index0 = findNewActiveIndex(event, activeId1, -1);
                        activeId0 = event.getPointerId(index0);
                    }

                    setContext(view, event);

                    pGestureInProgress = pListener.onScaleBegin(view, this);
                }
                break;

                case MotionEvent.ACTION_POINTER_UP: {
                    final int pointerCount = event.getPointerCount();
                    final int actionIndex = event.getActionIndex();
                    final int actionId = event.getPointerId(actionIndex);

                    boolean gestureEnd = false;
                    if (pointerCount > 2) {
                        if (actionId == activeId0) {
                            final int newIndex = findNewActiveIndex(event, activeId1, actionIndex);
                            if (newIndex >= 0) {
                                pListener.onScaleEnd(view, this);
                                activeId0 = event.getPointerId(newIndex);
                                activeMostRecent = true;
                                pEvent = MotionEvent.obtain(event);
                                setContext(view, event);
                                pGestureInProgress = pListener.onScaleBegin(view, this);
                            } else {
                                gestureEnd = true;
                            }
                        } else if (actionId == activeId1) {
                            final int newIndex = findNewActiveIndex(event, activeId0, actionIndex);
                            if (newIndex >= 0) {
                                pListener.onScaleEnd(view, this);
                                activeId1 = event.getPointerId(newIndex);
                                activeMostRecent = false;
                                pEvent = MotionEvent.obtain(event);
                                setContext(view, event);
                                pGestureInProgress = pListener.onScaleBegin(view, this);
                            } else {
                                gestureEnd = true;
                            }
                        }
                        pEvent.recycle();
                        pEvent = MotionEvent.obtain(event);
                        setContext(view, event);
                    } else {
                        gestureEnd = true;
                    }

                    if (gestureEnd) {
                        setContext(view, event);

                        // Set focus point to the remaining finger
                        final int activeId = actionId == activeId0 ? activeId1 : activeId0;
                        final int index = event.findPointerIndex(activeId);
                        focusX = event.getX(index);
                        focusY = event.getY(index);

                        pListener.onScaleEnd(view, this);
                        reset();
                        activeId0 = activeId;
                        activeMostRecent = true;
                    }
                }
                break;

                case MotionEvent.ACTION_CANCEL:
                    pListener.onScaleEnd(view, this);
                    reset();
                    break;

                case MotionEvent.ACTION_UP:
                    reset();
                    break;

                case MotionEvent.ACTION_MOVE: {
                    setContext(view, event);

                    // Only accept the event if our relative pressure is within
                    // a certain limit - this can help filter shaky data as a
                    // finger is lifted.
                    if (cPressure / pPressure > PRESSURE_THRESHOLD) {
                        final boolean updatePrevious = pListener.onScale(view, this);

                        if (updatePrevious) {
                            pEvent.recycle();
                            pEvent = MotionEvent.obtain(event);
                        }
                    }
                }
                break;
            }
        }

        return handle;
    }

    private int findNewActiveIndex(MotionEvent ev, int otherActiveId, int removedPointerIndex) {
        final int pointerCount = ev.getPointerCount();

        final int otherActiveIndex = ev.findPointerIndex(otherActiveId);

        // Pick a new id and update tracking state.
        for (int i = 0; i < pointerCount; i++) {
            if (i != removedPointerIndex && i != otherActiveIndex) {
                return i;
            }
        }
        return -1;
    }

    private void setContext(View view, MotionEvent curr) {
        if (cEvent != null) {
            cEvent.recycle();
        }
        cEvent = MotionEvent.obtain(curr);

        cLen = -1;
        pLen = -1;
        scaleFactor = -1;
        currSpanVector.set(0.0f, 0.0f);

        final MotionEvent prev = pEvent;

        final int pIndex0 = prev.findPointerIndex(activeId0);
        final int pIndex1 = prev.findPointerIndex(activeId1);
        final int cIndex0 = curr.findPointerIndex(activeId0);
        final int cIndex1 = curr.findPointerIndex(activeId1);

        if (pIndex0 < 0 || pIndex1 < 0 || cIndex0 < 0 || cIndex1 < 0) {
            invalidGesture = true;
            Log.e(TAG, "Invalid MotionEvent stream detected.", new Throwable());
            if (pGestureInProgress) {
                pListener.onScaleEnd(view, this);
            }
            return;
        }

        final float preX0 = prev.getX(pIndex0);
        final float preY0 = prev.getY(pIndex0);
        final float preX1 = prev.getX(pIndex1);
        final float preY1 = prev.getY(pIndex1);
        final float currX0 = curr.getX(cIndex0);
        final float currY0 = curr.getY(cIndex0);
        final float currX1 = curr.getX(cIndex1);
        final float currY1 = curr.getY(cIndex1);

        final float preX = preX1 - preX0;
        final float preY = preY1 - preY0;
        final float currX = currX1 - currX0;
        final float currY = currY1 - currY0;

        currSpanVector.set(currX, currY);

        pFingerDiffX = preX;
        pFingerDiffY = preY;
        cFingerDiffX = currX;
        cFingerDiffY = currY;

        focusX = currX0 + currX * 0.5f;
        focusY = currY0 + currY * 0.5f;
        mTimeDelta = curr.getEventTime() - prev.getEventTime();
        cPressure = curr.getPressure(cIndex0) + curr.getPressure(cIndex1);
        pPressure = prev.getPressure(pIndex0) + prev.getPressure(pIndex1);
    }

    private void reset() {
        if (pEvent != null) {
            pEvent.recycle();
            pEvent = null;
        }
        if (cEvent != null) {
            cEvent.recycle();
            cEvent = null;
        }
        pGestureInProgress = false;
        activeId0 = -1;
        activeId1 = -1;
        invalidGesture = false;
    }

    public boolean isInProgress() {
        return pGestureInProgress;
    }


    public float getFocusX() {
        return focusX;
    }

    public float getFocusY() {
        return focusY;
    }

    public float getCurrentSpan() {
        if (cLen == -1) {
            final float currVx = cFingerDiffX;
            final float currVy = cFingerDiffY;
            cLen = (float) Math.sqrt(currVx * currVx + currVy * currVy);
        }
        return cLen;
    }

    public Vector2D getCurrentSpanVector() {
        return currSpanVector;
    }


    public float getCurrentSpanX() {
        return cFingerDiffX;
    }

    public float getCurrentSpanY() {
        return cFingerDiffY;
    }


    public float getPreviousSpan() {
        if (pLen == -1) {
            final float preVx = pFingerDiffX;
            final float preVy = pFingerDiffY;
            pLen = (float) Math.sqrt(preVx * preVx + preVy * preVy);
        }
        return pLen;
    }


    public float getPreviousSpanX() {
        return pFingerDiffX;
    }

    public float getPreviousSpanY() {
        return pFingerDiffY;
    }


    public float getScaleFactor() {
        if (scaleFactor == -1) {
            scaleFactor = getCurrentSpan() / getPreviousSpan();
        }
        return scaleFactor;
    }

    public long getTimeDelta() {
        return mTimeDelta;
    }

    public long getEventTime() {
        return cEvent.getEventTime();
    }
}