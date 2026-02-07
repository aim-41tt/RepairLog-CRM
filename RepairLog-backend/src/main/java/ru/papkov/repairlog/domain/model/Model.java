package ru.papkov.repairlog.domain.model;

import jakarta.persistence.*;


/**
 *  класс для моделей устройств.
 * Примеры: iPhone 14 Pro, Galaxy S23, ThinkPad X1 и т.д.
 * Модель привязана к бренду.
 * 
 * @author aim-41tt
 */
@Entity
@Table(name = "models", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"name", "brand_id"})
})


public class Model {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Название модели.
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * Бренд устройства.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    /**
     * Получить полное название модели (Бренд + Модель).
     */
    public String getFullName() {
        return brand != null ? brand.getName() + " " + name : name;
    }

    /**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the brand
	 */
	public Brand getBrand() {
		return brand;
	}

	/**
	 * @param brand the brand to set
	 */
	public void setBrand(Brand brand) {
		this.brand = brand;
	}

	@Override
    public String toString() {
        return "Model{id=" + id + ", name='" + name + "', brandId=" + 
            (brand != null ? brand.getId() : null) + '}';
    }
}
