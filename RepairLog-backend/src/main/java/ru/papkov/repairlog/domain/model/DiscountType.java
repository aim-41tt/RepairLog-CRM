package ru.papkov.repairlog.domain.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

/**
 * класс для типов скидок. Скидка может быть либо процентной, либо фиксированной
 * суммой (не оба одновременно).
 * 
 * @author aim-41tt
 */
@Entity
@Table(name = "discount_types")
public class DiscountType {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * Название типа скидки.
	 */
	@Column(name = "name", nullable = false, unique = true, length = 50)
	private String name;

	/**
	 * Процент скидки (если скидка процентная).
	 */
	@Column(name = "percent", precision = 5, scale = 2)
	private BigDecimal percent;

	/**
	 * Фиксированная сумма скидки (если скидка фиксированная).
	 */
	@Column(name = "fixed_amount", precision = 12, scale = 2)
	private BigDecimal fixedAmount;

	/**
	 * Проверить, является ли скидка процентной.
	 */
	public boolean isPercentage() {
		return percent != null;
	}

	/**
	 * Проверить, является ли скидка фиксированной суммой.
	 */
	public boolean isFixedAmount() {
		return fixedAmount != null;
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
	 * @return the percent
	 */
	public BigDecimal getPercent() {
		return percent;
	}

	/**
	 * @param percent the percent to set
	 */
	public void setPercent(BigDecimal percent) {
		this.percent = percent;
	}

	/**
	 * @return the fixedAmount
	 */
	public BigDecimal getFixedAmount() {
		return fixedAmount;
	}

	/**
	 * @param fixedAmount the fixedAmount to set
	 */
	public void setFixedAmount(BigDecimal fixedAmount) {
		this.fixedAmount = fixedAmount;
	}

	@Override
	public String toString() {
		if (isPercentage()) {
			return "DiscountType{id=" + id + ", name='" + name + "', percent=" + percent + "%}";
		} else if (isFixedAmount()) {
			return "DiscountType{id=" + id + ", name='" + name + "', fixedAmount=" + fixedAmount + "}";
		}
		return "DiscountType{id=" + id + ", name='" + name + "'}";
	}
}
