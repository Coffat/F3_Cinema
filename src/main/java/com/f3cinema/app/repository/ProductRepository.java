package com.f3cinema.app.repository;

import com.f3cinema.app.entity.Product;

public interface ProductRepository extends BaseRepository<Product, Long> {
    void softDelete(Long productId);
}
