package ru.noties.scrollable.sample.next;

class SampleListItem {

    private final Class<?> sampleActivityClass;
    private final String title;
    private final String description;

    SampleListItem(Class<?> sampleActivityClass, String title, String description) {
        this.sampleActivityClass = sampleActivityClass;
        this.title = title;
        this.description = description;
    }

    Class<?> sampleActivityClass() {
        return sampleActivityClass;
    }

    String title() {
        return title;
    }

    String description() {
        return description;
    }

    @Override
    public String toString() {
        return "SampleListItem{" +
                "sampleActivityClass=" + sampleActivityClass +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
