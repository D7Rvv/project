import {
    ids,
    classIds
} from "./ids.js";
import * as helper from "./helper.js";
import type * as Types from "./types.js";

let initialized = false;

const PAGE_SIZE = 20;
const SLIDER_SEARCH_DELAY_MS = 350;
const CATEGORY_ORDER: Types.OptionType[] = ["cpu", "gpu", "memory", "mother_board", "ssd", "hdd"];

let latestSearchText = "";
let latestOptions: Types.OptionsResponse = {};
let sliderSearchTimerId: number | null = null;

function scheduleSliderSearch(): void {
    if (sliderSearchTimerId !== null) {
        window.clearTimeout(sliderSearchTimerId);
    }

    sliderSearchTimerId = window.setTimeout(() => {
        sliderSearchTimerId = null;
        void goToPage(1, true);
    }, SLIDER_SEARCH_DELAY_MS);
}

async function addOptionTab(): Promise<void> {
    const optionArea = getElement("optionAreaId");
    optionArea.innerHTML = "";

    const currentCategory = getCurrentCategory();

    try {
        const options = await helper.getOptions(currentCategory);
        latestOptions = options;

        if (currentCategory === "all") {
            const keys = CATEGORY_ORDER.filter((key) => options[key]);

            if (keys.length === 0) {
                optionArea.innerHTML = `<div style="padding:12px;border:1px solid #e2e8f0;border-radius:12px;background:#fff;color:#64748b;">利用できる絞り込み条件がありません。</div>`;
                return;
            }

            const commonState: Types.CommonRenderState = {
                makers: [],
                price: null
            };

            for (const categoryKey of keys) {
                const categoryOption = options[categoryKey];
                if (!categoryOption) {
                    continue;
                }

                collectCommonState(categoryKey, categoryOption, commonState);
            }

            const commonId = `${ids.optionAreaId}-common`;
            const commonArea = await helper.addArea(ids.optionAreaId, commonId, classIds.optionArea);
            appendCategoryLabel(commonArea, "all");

            await renderCommonControls(commonArea, commonState);

            for (const categoryKey of keys) {
                const categoryOption = options[categoryKey];
                if (!categoryOption) {
                    continue;
                }

                const groupId = `${ids.optionAreaId}-${categoryKey}`;
                const groupArea = await helper.addArea(ids.optionAreaId, groupId, classIds.optionArea);
                appendCategoryLabel(groupArea, categoryKey);
                await renderOptionControls(groupArea.id, categoryKey, categoryOption, true);
            }

            return;
        }

        const categoryOption = options[currentCategory];
        if (!categoryOption) {
            throw new Error(`カテゴリ "${currentCategory}" のオプションは取得できませんでした。`);
        }

        await renderOptionControls(ids.optionAreaId, currentCategory, categoryOption, false);
    } catch (error) {
        console.error("オプションの取得または表示に失敗:", error);
        optionArea.innerHTML = `<div style="padding:12px;border:1px solid #fecaca;border-radius:12px;background:#fff1f2;color:#b91c1c;">絞り込み条件の読み込みに失敗しました。</div>`;
    }
}

function collectCommonState(
    categoryKey: Types.OptionType,
    categoryOption: Types.ProductOptionResponse,
    commonState: Types.CommonRenderState
): void {
    collectCommonMakers(categoryKey, categoryOption, commonState);
    collectCommonPrice(categoryOption, commonState);
}

function collectCommonMakers(
    categoryKey: Types.OptionType,
    categoryOption: Types.ProductOptionResponse,
    commonState: Types.CommonRenderState
): void {
    const makerOptions = categoryOption.maker;

    if (!isSelectableOptionArray(makerOptions)) {
        return;
    }

    for (const option of makerOptions) {
        const makerValue = String(option.value).trim();
        const normalizedValue = makerValue.toLowerCase();
        const key = `${normalizedValue}：${categoryKey}`;

        const exists = commonState.makers.some((item) => item.key === key);
        if (exists) {
            continue;
        }

        commonState.makers.push({
            category: categoryKey,
            option,
            label: `${makerValue}：${categoryKey}`,
            key
        });
    }
}

function collectCommonPrice(
    categoryOption: Types.ProductOptionResponse,
    commonState: Types.CommonRenderState
): void {
    const price = categoryOption.price;

    if (!isRangeOption(price)) {
        return;
    }

    if (commonState.price === null) {
        commonState.price = {
            name: price.name,
            minValue: price.minValue,
            maxValue: price.maxValue,
            minLimit: price.minLimit,
            maxLimit: price.maxLimit
        };
        return;
    }

    commonState.price.minValue = Math.min(commonState.price.minValue, price.minValue);
    commonState.price.maxValue = Math.max(commonState.price.maxValue, price.maxValue);
    commonState.price.minLimit = Math.min(commonState.price.minLimit, price.minLimit);
    commonState.price.maxLimit = Math.max(commonState.price.maxLimit, price.maxLimit);
}

async function renderCommonControls(
    commonArea: HTMLDivElement,
    commonState: Types.CommonRenderState
): Promise<void> {
    if (commonState.makers.length > 0) {
        appendOptionLabel(commonArea, "maker");
        await appendCommonMakerSelector(commonArea, commonState.makers);
    }

    if (commonState.price !== null) {
        appendOptionLabel(commonArea, "price");
        await appendCommonPriceSelector(commonArea, commonState.price);
    }
}

async function renderOptionControls(
    areaId: string,
    categoryKey: Types.OptionType,
    categoryOption: Types.ProductOptionResponse,
    skipCommonOptions: boolean
): Promise<void> {
    for (const [optionName, optionValue] of Object.entries(categoryOption)) {
        if (optionName === "optionType" || typeof optionValue === "string") {
            continue;
        }

        if (skipCommonOptions && (optionName === "maker" || optionName === "price")) {
            continue;
        }

        const elementId = `${areaId}-${categoryKey}-${optionName}`;
        const optionChild = await helper.addArea(areaId, elementId, classIds.optionArea);
        appendOptionLabel(optionChild, optionName);
        await appendOptionSelecter(optionChild, optionValue);
    }
}

async function appendCommonMakerSelector(
    optionChild: HTMLElement,
    makerItems: Types.CommonMakerItem[]
): Promise<void> {
    const optionValueIdPrefix = `${optionChild.id}-common-maker-`;

    for (const item of makerItems) {
        await helper.addCheckbox(
            `${optionValueIdPrefix}${item.key}`,
            item.label,
            optionChild.id,
            classIds.checkbox,
            async (newValue: boolean) => {
                item.option.isSelected = newValue;
                await goToPage(1, true);
            }
        );
    }
}

async function appendCommonPriceSelector(
    optionChild: HTMLElement,
    priceOption: Types.RangeOption
): Promise<void> {
    const optionValueId = `${optionChild.id}-${priceOption.name}`;

    await helper.addNoUiSlider(
        optionValueId,
        priceOption.minValue,
        priceOption.maxValue,
        optionChild.id,
        classIds.doubleSlider,
        async (minValue: number, maxValue: number) => {
            priceOption.minValue = minValue;
            priceOption.maxValue = maxValue;

            applyPriceToAllCategories(minValue, maxValue);

            scheduleSliderSearch();
        }
    );
}

function applyPriceToAllCategories(minValue: number, maxValue: number): void {
    for (const categoryKey of CATEGORY_ORDER) {
        const categoryOption = latestOptions[categoryKey];
        if (!categoryOption || !isRangeOption(categoryOption.price)) {
            continue;
        }

        categoryOption.price.minValue = Math.min(minValue, maxValue);
        categoryOption.price.maxValue = Math.max(minValue, maxValue);
    }
}

async function appendOptionSelecter(
    optionChild: HTMLElement,
    optionValue: Types.SelectableOption[] | Types.RangeOption
): Promise<void> {
    if (isSelectableOptionArray(optionValue)) {
        const optionValueIdPrefix = `${optionChild.id}-`;

        for (const option of optionValue) {
            await helper.addCheckbox(
                `${optionValueIdPrefix}${option.name}`,
                option.value,
                optionChild.id,
                classIds.checkbox,
                async (newValue: boolean) => {
                    option.isSelected = newValue;
                    await goToPage(1, true);
                }
            );
        }

        return;
    }

    if (isRangeOption(optionValue)) {
        const optionValueId = `${optionChild.id}-${optionValue.name}`;

        await helper.addNoUiSlider(
            optionValueId,
            optionValue.minValue,
            optionValue.maxValue,
            optionChild.id,
            classIds.doubleSlider,
            async (minValue: number, maxValue: number) => {
                optionValue.minValue = minValue;
                optionValue.maxValue = maxValue;
                scheduleSliderSearch();
            }
        );
    }
}

function appendCategoryLabel(parent: HTMLElement, category: Types.OptionTypeWithAll): void {
    const title = document.createElement("div");
    title.textContent = formatCategoryLabel(category);
    title.className = `${classIds.categoryOption}`;
    parent.appendChild(title);
}

function appendOptionLabel(parent: HTMLElement, optionName: string): void {
    const title = document.createElement("div");
    title.textContent = formatOptionLabel(optionName);
    title.className = `${classIds.categoryOption}`;
    parent.appendChild(title);
}

function formatCategoryLabel(category: Types.OptionTypeWithAll): string {
    switch (category) {
        case "all": return "共通";
        case "cpu": return "CPU";
        case "gpu": return "GPU";
        case "memory": return "メモリ";
        case "mother_board": return "マザーボード";
        case "ssd": return "SSD";
        case "hdd": return "HDD";
    }
}

function formatOptionLabel(optionName: string): string {
    return optionName
        .replace(/_/g, " ")
        .replace(/\s+/g, " ")
        .trim()
        .toUpperCase();
}

function getElement(key: keyof typeof ids): HTMLElement {
    const id = ids[key];
    if (!id) throw new Error(`ID:"${key}"が定義されていません。`);
    const element = document.getElementById(id);
    if (!element) throw new Error(`ID:"${id}"を持つ要素が見つかりません。`);
    return element;
}

function getCurrentCategory(): Types.OptionTypeWithAll {
    const currentCategoryElement = getElement("currentCategoryId");
    const currentCategory = currentCategoryElement.textContent?.trim().toLowerCase();

    if (!currentCategory) {
        throw new Error("currentCategoryIdの要素からカテゴリが取得できませんでした。");
    }
    if (!isOptionTypeWithAll(currentCategory)) {
        throw new Error(`無効なカテゴリ: ${currentCategory}`);
    }

    return currentCategory;
}

function setCurrentCategory(category: Types.OptionTypeWithAll): void {
    getElement("currentCategoryId").textContent = category;

    const selector = document.getElementById(ids.categorySelectorId) as HTMLSelectElement | null;
    if (selector) {
        selector.value = category;
    }

    const label = document.getElementById(ids.currentCategoryLabelId);
    if (label) {
        label.textContent = formatVisibleCategory(category);
    }

    const heroName = document.getElementById(ids.heroCategoryNameId);
    if (heroName) {
        heroName.textContent = formatVisibleCategory(category);
    }
}

function formatVisibleCategory(category: Types.OptionTypeWithAll): string {
    switch (category) {
        case "all": return "すべて";
        case "cpu": return "CPU";
        case "gpu": return "GPU";
        case "memory": return "メモリ";
        case "mother_board": return "マザーボード";
        case "ssd": return "SSD";
        case "hdd": return "HDD";
    }
}

function normalizePage(page: number): number {
    if (!Number.isFinite(page)) return 1;
    return Math.max(1, Math.floor(page));
}

function getCurrentPageFromUrl(): number {
    const url = new URL(window.location.href);
    const pageText = url.searchParams.get("page");
    if (!pageText) return 1;

    const parsed = Number.parseInt(pageText, 10);
    if (!Number.isFinite(parsed) || parsed < 1) return 1;
    return parsed;
}

function getCategoryFromUrl(): Types.OptionTypeWithAll | null {
    const url = new URL(window.location.href);
    const category = url.searchParams.get("category")?.trim().toLowerCase();
    if (!category) return null;
    return isOptionTypeWithAll(category) ? category : null;
}

function updatePageUrl(page: number, replace: boolean = false): void {
    const safePage = normalizePage(page);
    const url = new URL(window.location.href);

    url.searchParams.set("page", String(safePage));
    url.searchParams.set("category", getCurrentCategory());

    if (replace) {
        window.history.replaceState({ page: safePage, category: getCurrentCategory() }, "", `${url.pathname}${url.search}${url.hash}`);
    } else {
        window.history.pushState({ page: safePage, category: getCurrentCategory() }, "", `${url.pathname}${url.search}${url.hash}`);
    }
}

async function goToPage(page: number, replace: boolean = false): Promise<void> {
    const safePage = normalizePage(page);
    updatePageUrl(safePage, replace);
    await search({
        option: latestOptions,
        page: safePage
    });
}

function renderPagination(currentPage: number, totalPages: number): void {
    const pageArea = getElement("pageAreaId");
    const currentPageElement = getElement("currentPage");

    pageArea.innerHTML = "";

    const prevButton = document.createElement("button");
    prevButton.textContent = "前へ";
    prevButton.className = `${classIds.pageButton}`;
    prevButton.disabled = currentPage <= 1;
    prevButton.addEventListener("click", () => {
        void goToPage(currentPage - 1);
    });

    currentPageElement.textContent = `${currentPage} / ${Math.max(totalPages, 1)}`;
    currentPageElement.className = `${classIds.activePageButton}`;

    const nextButton = document.createElement("button");
    nextButton.textContent = "次へ";
    nextButton.className = `${classIds.pageButton}`;
    nextButton.disabled = currentPage >= totalPages;
    nextButton.addEventListener("click", () => {
        void goToPage(currentPage + 1);
    });

    pageArea.appendChild(prevButton);
    pageArea.appendChild(currentPageElement);
    pageArea.appendChild(nextButton);
}

async function search({
    option,
    page
}: {
    option?: Types.OptionsResponse;
    page?: number;
}): Promise<void> {
    try {
        const searchInput = getElement("searchInputId") as HTMLInputElement | null;
        if (searchInput) {
            latestSearchText = searchInput.value.trim();
        } else if (!latestSearchText) {
            latestSearchText = "";
        }

        latestSearchText = latestSearchText;
        latestOptions = option ?? latestOptions ?? {};

        const currentCategory = getCurrentCategory();
        const productDisplayArea = getElement("productAreaId");
        const targetPage = normalizePage(page ?? getCurrentPageFromUrl());

        const result = await helper.search(
            latestSearchText,
            currentCategory,
            latestOptions,
            targetPage,
            PAGE_SIZE
        );

        const products = result.products ?? [];
        const totalPages = Math.max(1, result.info?.totalPage ?? 1);
        const currentPage = normalizePage(result.info?.resultPage ?? targetPage);

        productDisplayArea.innerHTML = "";

        products.forEach((product) => {
            helper.addProductCard(product, productDisplayArea.id, classIds.productCard);
        });

        if (products.length === 0) {
            productDisplayArea.innerHTML = `<div class="${classIds.productCard}">条件に一致する商品がありませんでした。</div>`;
        }

        renderPagination(currentPage, totalPages);

        if (currentPage !== getCurrentPageFromUrl()) {
            updatePageUrl(currentPage, true);
        }
    } catch (error) {
        console.error("検索に失敗:", error);
    }
}

async function handleCategoryChange(newCategory: Types.OptionTypeWithAll): Promise<void> {
    if (sliderSearchTimerId !== null) {
        window.clearTimeout(sliderSearchTimerId);
        sliderSearchTimerId = null;
    }

    setCurrentCategory(newCategory);
    latestOptions = {};
    await addOptionTab();
    await goToPage(1, true);
}

function setupCategorySelector(): void {
    const selector = document.getElementById(ids.categorySelectorId) as HTMLSelectElement | null;
    if (!selector) {
        return;
    }

    selector.addEventListener("change", () => {
        const value = selector.value.trim().toLowerCase();
        if (!isOptionTypeWithAll(value)) {
            return;
        }
        void handleCategoryChange(value);
    });
}

async function main(): Promise<void> {
    if (initialized) {
        return;
    }
    initialized = true;

    try {
        helper.initImageErrorHandlers(); // 画像エラーハンドリングを初期化

        const initialCategory = getCategoryFromUrl() ?? "all";
        setCurrentCategory(initialCategory);
        setupCategorySelector();

        await addOptionTab();
        await search({ page: getCurrentPageFromUrl() });

        window.addEventListener("popstate", () => {
            const categoryFromUrl = getCategoryFromUrl() ?? "all";
            setCurrentCategory(categoryFromUrl);
            void addOptionTab().then(() => search({ page: getCurrentPageFromUrl() }));
        });
    } catch (error) {
        console.error("初期化に失敗:", error);
    }
}

function isOptionType(value: string): value is Types.OptionType {
    return CATEGORY_ORDER.includes(value as Types.OptionType);
}

function isOptionTypeWithAll(value: string): value is Types.OptionTypeWithAll {
    return value === "all" || isOptionType(value);
}

function isSelectableOptionArray(value: unknown): value is Types.SelectableOption[] {
    return Array.isArray(value) &&
        value.every((item) =>
            typeof item === "object" &&
            item !== null &&
            "name" in item &&
            "value" in item &&
            "isSelected" in item
        );
}

function isRangeOption(value: unknown): value is Types.RangeOption {
    return (
        typeof value === "object" &&
        value !== null &&
        "name" in value &&
        "minValue" in value &&
        "maxValue" in value &&
        "minLimit" in value &&
        "maxLimit" in value
    );
}



if (document.readyState === "loading") {
    window.addEventListener("DOMContentLoaded", () => void main(), { once: true });
} else {
    void main();
}

//検索ボタンのクリックイベントを追加
getElement("searchButtonId").addEventListener("click", () => void search({}));