package ulm.university.news.app.util;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import ulm.university.news.app.R;

/**
 * TODO
 *
 * @author Matthias Mak
 */
public class TextInputLabels extends LinearLayout {

    private Context context;

    private TextView tvName;
    private TextView tvError;
    private EditText etText;

    public TextInputLabels(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public TextInputLabels(Context context) {
        super(context);
        this.context = context;
        init();
    }

    private void init() {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.text_input_labels, this, true);
        tvName = (TextView) view.findViewById(R.id.text_input_labels_tv_name);
        tvError = (TextView) view.findViewById(R.id.text_input_labels_tv_error);
        etText = (EditText) view.findViewById(R.id.text_input_labels_et_text);

        etText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                hideError();
                if (s.toString().length() == 0) {
                    tvName.setVisibility(INVISIBLE);
                }
            }
        });
    }

    public void setName(String name) {
        tvName.setText(name);
    }

    public void setHint(String hint){
        etText.setHint(hint);
    }

    public void setNameAndHint(String nameAndHint){
        tvName.setText(nameAndHint);
        etText.setHint(nameAndHint);
    }

    public void showError(String error) {
        tvError.setText(error);
        tvError.setVisibility(VISIBLE);
        tvName.setVisibility(GONE);
        etText.getBackground().setColorFilter(ContextCompat.getColor(context, R.color.error), PorterDuff.Mode.SRC_ATOP);
    }

    public void hideError() {
        tvError.setVisibility(GONE);
        tvName.setVisibility(VISIBLE);
        // TODO Properly reset etText color.
        etText.getBackground().clearColorFilter();
    }
}
