package flc.upload.model;

public class Task {
    private Integer id;
    private String text;
    private boolean checked;

    public Task(String text, boolean checked) {
        this.text = text;
        this.checked = checked;
    }

    public Task(Integer id, String text, boolean checked) {
        this.id = id;
        this.text = text;
        this.checked = checked;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
