package ulm.university.news.app.util;

import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
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
    /** This classes tag for logging. */
    private static final String TAG = "TextInputLabels";

    private Context context;

    private TextView tvName;
    private TextView tvLength;
    private TextView tvError;
    private EditText etText;

    private int minLength;
    private int maxLength;
    private String pattern;
    private String error;

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
        tvLength = (TextView) view.findViewById(R.id.text_input_labels_tv_length);
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
                int length = s.toString().length();
                if (length == 0) {
                    tvName.setVisibility(INVISIBLE);
                    tvLength.setVisibility(INVISIBLE);
                } else {
                    tvLength.setText(length + "/" + maxLength);
                }
            }
        });
    }

    public void setToPasswordField() {
        etText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        etText.setTypeface(tvName.getTypeface());
    }

    public void setToNumberField() {
        etText.setInputType(InputType.TYPE_NUMBER_FLAG_SIGNED);
    }

    public void setName(String name) {
        tvName.setText(name);
    }

    public void setHint(String hint) {
        etText.setHint(hint);
    }

    public void setNameAndHint(String nameAndHint) {
        tvName.setText(nameAndHint);
        etText.setHint(nameAndHint);
    }

    public String getText() {
        return etText.getText().toString().trim();
    }

    public void setError(String error) {
        tvError.setText(error);
        this.error = error;
    }

    public void hideError() {
        tvError.setVisibility(GONE);
        tvName.setVisibility(VISIBLE);
        tvLength.setVisibility(VISIBLE);
    }

    public void setLength(int minLength, int maxLength) {
        this.minLength = minLength;
        this.maxLength = maxLength;
        etText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public boolean isValid() {
        int length = etText.getText().length();
        // If a pattern is set, validate via pattern.
        if (pattern != null) {
            boolean valid = etText.getText().toString().matches(pattern);
            if (!valid) {
                showError();
            }
            return valid;
        }
        // If max length is set, validate via text length.
        if (maxLength != 0 && (length < minLength || length > maxLength)) {
            showError();
            return false;
        }
        // If nothing is set, just return true.
        return true;
    }

    private void showError() {
        // If field is empty, show empty error.
        if (etText.getText().length() == 0) {
            tvError.setText(tvName.getText() + " " + context.getString(R.string.general_error_input_field_empty));
        } else {
            // Otherwise, if no custom error message was set, use default using the fields name.
            if (error == null) {
                tvError.setText(tvName.getText() + " " + context.getString(R.string.general_error_input_field_invalid));
            } else {
                tvError.setText(error);
            }
        }

        tvError.setVisibility(VISIBLE);
        tvName.setVisibility(GONE);
        tvLength.setVisibility(GONE);

        // Android bugs in color filter. Wait for fixed version update.
        // etText.getBackground().setColorFilter(ContextCompat.getColor(context, R.color.error), PorterDuff.Mode
        // .SRC_ATOP);
    }

    public void showError(String error) {
        tvError.setText(error);
        tvError.setVisibility(VISIBLE);
        tvName.setVisibility(GONE);
        tvLength.setVisibility(GONE);
    }
}
