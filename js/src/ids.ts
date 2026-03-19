/**HTMLのID */

import { search } from "./helper";

export const ids = {
    example: "example",
    optionAreaId: "filter-option-area",
    productAreaId: "product-display-area",
    searchInputId: "search-input",
    searchButtonId: "search-button",
    pageAreaId: "page-area",
    currentCategoryId: "current-category",
    currentPage: "current-page",
    categorySelectorId: "category-select",
    currentCategoryLabelId: "current-category-label",
    heroCategoryNameId: "hero-category-name"
} satisfies Record<string, string>;

export const classIds = {
    productCard: {
        base: "product-card",
        imageWrap: "product-card-image-wrap",
        image: "product-card-image",
        name: "product-card-name",
        maker: "product-card-maker",
        spec: "product-card-spec",
        priceBlock: "product-card-price-block",
        price: "product-card-price",
        tax: "product-card-tax",
        detailHint: "product-card-detail-hint"
    },
    optionArea: "option-area",
    optionChild: "option-child",
    checkbox: "option-checkbox",
    doubleSlider: "option-double-slider",
    pageButton: "page-button",
    activePageButton: "active-page-button",
    categoryOption: "category-option"
} satisfies Record<string, string | Record<string, string>>;