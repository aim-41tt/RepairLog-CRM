package ru.papkov.repairlog.application.dto.common;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Обобщённый DTO для постраничных ответов.
 *
 * @param <T> тип элемента
 * @author aim-41tt
 */
public class PageResponse<T> {

	private List<T> content;
	private int page;
	private int size;
	private long totalElements;
	private int totalPages;
	private boolean last;

	public PageResponse() {
	}

	public PageResponse(List<T> content, int page, int size, long totalElements, int totalPages, boolean last) {
		this.content = content;
		this.page = page;
		this.size = size;
		this.totalElements = totalElements;
		this.totalPages = totalPages;
		this.last = last;
	}

	/**
	 * Построить PageResponse из Spring Data Page, преобразуя элементы через указанную функцию-маппер.
	 *
	 * @param page   страница сущностей
	 * @param mapper функция преобразования E -> D
	 * @param <E>    тип сущности
	 * @param <D>    тип DTO
	 * @return DTO-страница
	 */
	public static <E, D> PageResponse<D> of(Page<E> page, Function<E, D> mapper) {
		return new PageResponse<>(
				page.getContent().stream().map(mapper).toList(),
				page.getNumber(),
				page.getSize(),
				page.getTotalElements(),
				page.getTotalPages(),
				page.isLast()
		);
	}

	public List<T> getContent() {
		return content;
	}

	public void setContent(List<T> content) {
		this.content = content;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public long getTotalElements() {
		return totalElements;
	}

	public void setTotalElements(long totalElements) {
		this.totalElements = totalElements;
	}

	public int getTotalPages() {
		return totalPages;
	}

	public void setTotalPages(int totalPages) {
		this.totalPages = totalPages;
	}

	public boolean isLast() {
		return last;
	}

	public void setLast(boolean last) {
		this.last = last;
	}
}
