import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * A slider with two thumbs bounding a selected range
 */
public class RangeSlider extends JComponent {

    private static final int THUMB_RADIUS = 8;
    private static final int TRACK_HEIGHT = 6;

    private final int minimum;
    private final int maximum;
    private int lowValue;
    private int highValue;

    private Thumb draggedThumb;

    private enum Thumb { LOW, HIGH }

    public RangeSlider(int minimum, int maximum) {
        this.minimum = minimum;
        this.maximum = maximum;
        this.lowValue = minimum;
        this.highValue = maximum;

        setOpaque(false);
        setFocusable(true);
        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                draggedThumb = nearestThumb(e.getX());
                dragTo(e.getX());
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                dragTo(e.getX());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                draggedThumb = null;
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
    }

    public int getLowValue() {
        return lowValue;
    }

    public int getHighValue() {
        return highValue;
    }

    public void setValues(int low, int high) {
        int clampedLow = Math.max(minimum, Math.min(low, maximum));
        int clampedHigh = Math.max(minimum, Math.min(high, maximum));
        this.lowValue = Math.min(clampedLow, clampedHigh);
        this.highValue = Math.max(clampedLow, clampedHigh);
        repaint();
        fireStateChanged();
    }

    public void addChangeListener(ChangeListener listener) {
        listenerList.add(ChangeListener.class, listener);
    }

    private void fireStateChanged() {
        ChangeEvent event = new ChangeEvent(this);
        for (ChangeListener listener : listenerList.getListeners(ChangeListener.class)) {
            listener.stateChanged(event);
        }
    }

    private Thumb nearestThumb(int x) {
        return Math.abs(x - valueToX(lowValue)) <= Math.abs(x - valueToX(highValue))
                ? Thumb.LOW
                : Thumb.HIGH;
    }

    private void dragTo(int x) {
        if (draggedThumb == null) {
            return;
        }
        int value = xToValue(x);
        if (draggedThumb == Thumb.LOW) {
            setValues(Math.min(value, highValue), highValue);
        } else {
            setValues(lowValue, Math.max(value, lowValue));
        }
    }

    private int trackLeft() {
        return THUMB_RADIUS;
    }

    private int trackWidth() {
        return Math.max(1, getWidth() - 2 * THUMB_RADIUS);
    }

    private int valueToX(int value) {
        double ratio = (value - minimum) / (double) (maximum - minimum);
        return trackLeft() + (int) Math.round(ratio * trackWidth());
    }

    private int xToValue(int x) {
        double ratio = (x - trackLeft()) / (double) trackWidth();
        long value = Math.round(minimum + ratio * (maximum - minimum));
        return (int) Math.max(minimum, Math.min(value, maximum));
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(600, 2 * THUMB_RADIUS + 8);
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int trackY = (getHeight() - TRACK_HEIGHT) / 2;
        int lowX = valueToX(lowValue);
        int highX = valueToX(highValue);

        g2.setColor(getBackground().darker());
        g2.fillRoundRect(trackLeft(), trackY, trackWidth(), TRACK_HEIGHT, TRACK_HEIGHT, TRACK_HEIGHT);

        g2.setColor(new Color(0x2D7FF9));
        g2.fillRoundRect(lowX, trackY, Math.max(1, highX - lowX), TRACK_HEIGHT, TRACK_HEIGHT, TRACK_HEIGHT);

        paintThumb(g2, lowX);
        paintThumb(g2, highX);

        g2.dispose();
    }

    private void paintThumb(Graphics2D g2, int centerX) {
        int diameter = 2 * THUMB_RADIUS;
        int y = (getHeight() - diameter) / 2;
        g2.setColor(Color.WHITE);
        g2.fillOval(centerX - THUMB_RADIUS, y, diameter, diameter);
        g2.setColor(new Color(0x1B5DBE));
        g2.drawOval(centerX - THUMB_RADIUS, y, diameter, diameter);
    }
}
