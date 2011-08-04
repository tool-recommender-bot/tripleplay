//
// Triple Play - utilities for use in ForPlay-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import forplay.core.Canvas;
import forplay.core.CanvasLayer;
import forplay.core.ForPlay;
import forplay.core.TextFormat;
import forplay.core.TextLayout;

import pythagoras.f.Dimension;

import react.Signal;

/**
 * A button that displays text, or an icon, or both.
 */
public class Button extends TextWidget
{
    /** A signal that is emitted when this button is clicked. */
    public final Signal<Button> click = Signal.create();

    /**
     * Creates a button with no custom styles.
     */
    public Button () {
    }

    /**
     * Creates a button with the specified custom styles.
     */
    public Button (Styles styles) {
        setStyles(styles);
    }

    /**
     * Returns the currently configured text, or null if the button does not use text.
     */
    @Override public String text () {
        return _text;
    }

    /**
     * Sets the text of this button to the supplied value.
     */
    @Override public Button setText (String text) {
        super.setText(text);
        return this;
    }

    @Override public String toString () {
        return "Button(" + _text + ")";
    }

    @Override protected void wasRemoved () {
        super.wasRemoved();
        // clear out our background instance
        if (_bginst != null) {
            _bginst.destroy();
            _bginst = null;
        }
        // if we're added again, we'll be re-laid-out
    }

    @Override protected Dimension computeSize (float hintX, float hintY) {
        Dimension size = new Dimension();
        LayoutData ldata = computeLayout(hintX, hintY);
        if (ldata.text != null) {
            size.width += ldata.text.width();
            size.height += ldata.text.height();
        }
        // TODO: if we have an icon, add that into the mix
        return ldata.bg.addInsets(size);
    }

    @Override protected void layout () {
        float width = _size.width, height = _size.height;
        LayoutData ldata = computeLayout(width, height);

        // prepare our background
        Background bg = _ldata.bg;
        if (_bginst != null) _bginst.destroy();
        _bginst = bg.instantiate(_size);
        _bginst.addTo(layer);
        width -= bg.width();
        height -= bg.height();

        // prepare our label
        renderLayout(_ldata, bg.left, bg.top, width, height);

        _ldata = null; // we no longer need our layout data
    }

    @Override protected void onPointerStart (float x, float y) {
        super.onPointerStart(x, y);
        set(Flag.DOWN, true);
        invalidate();
    }

    @Override protected void onPointerDrag (float x, float y) {
        super.onPointerDrag(x, y);
        boolean down = contains(x, y);
        if (down != isSet(Flag.DOWN)) {
            set(Flag.DOWN, down);
            invalidate();
        }
    }

    @Override protected void onPointerEnd (float x, float y) {
        super.onPointerEnd(x, y);
        // we don't check whether the supplied coordinates are in our bounds or not because only
        // the drag changes result in changes to the button's visualization, and we want to behave
        // based on what the user sees
        if (isSet(Flag.DOWN)) {
            set(Flag.DOWN, false);
            invalidate();
            click.emit(this); // emit a click event
        }
    }

    @Override protected State state () {
        State sstate = super.state();
        switch (sstate) {
        case DEFAULT: return isSet(Flag.DOWN) ? State.DOWN : State.DEFAULT;
        default:      return sstate;
        }
    }

    protected LayoutData computeLayout (float hintX, float hintY) {
        if (_ldata != null) return _ldata;
        _ldata = new LayoutData();

        // determine our background
        Background bg = resolveStyle(state(), Style.BACKGROUND);
        hintX -= bg.width();
        hintY -= bg.height();
        _ldata.bg = bg;

        // layout our text
        layoutText(_ldata, _text, hintX, hintY);

        return _ldata;
    }

    protected static class LayoutData extends TextWidget.LayoutData {
        public Background bg;
    }

    protected Background.Instance _bginst;
    protected LayoutData _ldata;
}
