package lk.jiat.xpect.model;

import java.util.ArrayList;

public class Category {
    private int id;
    private String name;

    public Category(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static ArrayList<Category> getSampleCategoryList(){
        ArrayList<Category> arrayList = new ArrayList<>();
        arrayList.add(new Category(1,"Music"));
        arrayList.add(new Category(1,"Moview"));
        arrayList.add(new Category(1,"Drama"));
        arrayList.add(new Category(1,"Concert"));
        return arrayList;
    }

}
