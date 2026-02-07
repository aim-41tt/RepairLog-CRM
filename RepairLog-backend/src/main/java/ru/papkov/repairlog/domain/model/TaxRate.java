package ru.papkov.repairlog.domain.model;

import jakarta.persistence.*;


import java.math.BigDecimal;

/**
 *  класс для налоговых ставок.
 * Например: НДС 20%, без НДС и т.д.
 * 
 * @author aim-41tt
 */
@Entity
@Table(name = "tax_rates")
public class TaxRate extends BaseEntity {
	
	private static final long serialVersionUID = 1L;

	/**
     * Название налоговой ставки.
     */
    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    /**
     * Процент налога.
     */
    @Column(name = "percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal percent;

    /**
     * Флаг активности ставки.
     */
    @Column(name = "is_active", nullable = false)
    
    private Boolean isActive = true;

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
	 * @return the isActive
	 */
	public Boolean getIsActive() {
		return isActive;
	}

	/**
	 * @param isActive the isActive to set
	 */
	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	@Override
    public String toString() {
        return "TaxRate{id=" + getId() + ", name='" + name + "', percent=" + percent + "%, isActive=" + isActive + '}';
    }
}
