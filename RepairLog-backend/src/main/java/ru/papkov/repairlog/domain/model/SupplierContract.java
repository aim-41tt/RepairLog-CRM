package ru.papkov.repairlog.domain.model;

import jakarta.persistence.*;


import java.time.LocalDate;

/**
 *  класс для договоров с поставщиками.
 * Позволяет отслеживать действующие и истёкшие контракты.
 * 
 * @author aim-41tt
 */
@Entity
@Table(name = "supplier_contracts")
public class SupplierContract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Поставщик.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    /**
     * Номер договора.
     */
    @Column(name = "contract_number", nullable = false, unique = true, length = 100)
    private String contractNumber;

    /**
     * Дата подписания договора.
     */
    @Column(name = "signed_at", nullable = false)
    private LocalDate signedAt;

    /**
     * Дата окончания действия договора.
     */
    @Column(name = "valid_until")
    private LocalDate validUntil;

    /**
     * Флаг активности договора.
     */
    @Column(name = "is_active", nullable = false)
    
    private Boolean isActive = true;

    /**
     * Проверить, действителен ли договор на текущую дату.
     */
    public boolean isValid() {
        if (!isActive) {
            return false;
        }
        if (validUntil == null) {
            return true; // Бессрочный договор
        }
        return !validUntil.isBefore(LocalDate.now());
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
	 * @return the supplier
	 */
	public Supplier getSupplier() {
		return supplier;
	}

	/**
	 * @param supplier the supplier to set
	 */
	public void setSupplier(Supplier supplier) {
		this.supplier = supplier;
	}

	/**
	 * @return the contractNumber
	 */
	public String getContractNumber() {
		return contractNumber;
	}

	/**
	 * @param contractNumber the contractNumber to set
	 */
	public void setContractNumber(String contractNumber) {
		this.contractNumber = contractNumber;
	}

	/**
	 * @return the signedAt
	 */
	public LocalDate getSignedAt() {
		return signedAt;
	}

	/**
	 * @param signedAt the signedAt to set
	 */
	public void setSignedAt(LocalDate signedAt) {
		this.signedAt = signedAt;
	}

	/**
	 * @return the validUntil
	 */
	public LocalDate getValidUntil() {
		return validUntil;
	}

	/**
	 * @param validUntil the validUntil to set
	 */
	public void setValidUntil(LocalDate validUntil) {
		this.validUntil = validUntil;
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
        return "SupplierContract{" +
            "id=" + id +
            ", contractNumber='" + contractNumber + '\'' +
            ", supplierId=" + (supplier != null ? supplier.getId() : null) +
            ", isActive=" + isActive +
            ", validUntil=" + validUntil +
            '}';
    }
}
