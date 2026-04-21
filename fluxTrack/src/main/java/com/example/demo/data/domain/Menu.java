package com.example.demo.data.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.List;

@Entity
public class Menu{

    @Id
    @JsonIgnore
    private Long menuID;

    @OneToMany(mappedBy = "menu_id")
    private List<Product> productList;

    public Long getMenuID() {
        return menuID;
    }

    public void setMenuID(Long menuID) {
        this.menuID = menuID;
    }

    public List<Product> getProductList() {
        return productList;
    }

    public void setProductList(List<Product> productList) {
        this.productList = productList;
    }
}