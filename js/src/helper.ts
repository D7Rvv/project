import { classIds } from "./ids.js";
import type {
    CpuOption,
    GpuOption,
    HddOption,
    MemoryOption,
    MotherBoardOption,
    OptionType,
    OptionTypeWithAll,
    OptionsResponse,
    RangeOption,
    SelectableOption,
    SsdOption,
    Product,
    SearchResponse
} from "./types.js";

//定数
const API_URL = "/project/Servlet/API/";
const OPTIONS_API_URL = `${API_URL}Option`;

type ValidationResult =
    | { ok: true }
    | { ok: false; message: string };

const OPTION_KEYS: OptionType[] = [
    "cpu",
    "gpu",
    "memory",
    "mother_board",
    "ssd",
    "hdd"
];

function ok(): ValidationResult {
    return { ok: true };
}

function ng(message: string): ValidationResult {
    return { ok: false, message };
}

function isObject(value: unknown): value is Record<string, unknown> {
    return typeof value === "object" && value !== null && !Array.isArray(value);
}

function isString(value: unknown): value is string {
    return typeof value === "string";
}

function isBoolean(value: unknown): value is boolean {
    return typeof value === "boolean";
}

function isNumber(value: unknown): value is number {
    return typeof value === "number" && Number.isFinite(value);
}

function hasOnlyAllowedTopLevelKeys(obj: Record<string, unknown>): boolean {
    return Object.keys(obj).every((key) => OPTION_KEYS.includes(key as OptionType));
}

function validateSelectableOption(value: unknown, path: string): ValidationResult {
    if (!isObject(value)) {
        return ng(`${path} は object ではありません`);
    }

    if (!isString(value.name)) {
        return ng(`${path}.name は string ではありません`);
    }

    if (!isString(value.value)) {
        return ng(`${path}.value は string ではありません`);
    }

    if (!isBoolean(value.isSelected)) {
        return ng(`${path}.isSelected は boolean ではありません`);
    }

    return ok();
}

function validateSelectableOptionArray(value: unknown, path: string): ValidationResult {
    if (!Array.isArray(value)) {
        return ng(`${path} は配列ではありません`);
    }

    for (let i = 0; i < value.length; i++) {
        const result = validateSelectableOption(value[i], `${path}[${i}]`);
        if (!result.ok) {
            return result;
        }
    }

    return ok();
}

function validateRangeOption(value: unknown, path: string): ValidationResult {
    if (!isObject(value)) {
        return ng(`${path} は object ではありません`);
    }

    if (!isString(value.name)) {
        return ng(`${path}.name は string ではありません`);
    }

    if (!isNumber(value.minValue)) {
        return ng(`${path}.minValue は number ではありません`);
    }

    if (!isNumber(value.maxValue)) {
        return ng(`${path}.maxValue は number ではありません`);
    }

    if (!isNumber(value.maxLimit)) {
        return ng(`${path}.maxLimit は number ではありません`);
    }

    if (!isNumber(value.minLimit)) {
        return ng(`${path}.minLimit は number ではありません`);
    }

    return ok();
}

function validateProductOption(value: unknown, path: string): ValidationResult {
    if (!isObject(value)) {
        return ng(`${path} は object ではありません`);
    }

    for (const [key, child] of Object.entries(value)) {
        if (key === "optionType") {
            if (!isString(child)) {
                return ng(`${path}.optionType は string ではありません`);
            }
            continue;
        }

        if (Array.isArray(child)) {
            const result = validateSelectableOptionArray(child, `${path}.${key}`);
            if (!result.ok) return result;
            continue;
        }

        if (isObject(child)) {
            const result = validateRangeOption(child, `${path}.${key}`);
            if (!result.ok) return result;
            continue;
        }

        return ng(`${path}.${key} の形式が不正です`);
    }

    return ok();
}

function validateOptionsResponse(
    value: unknown,
    requestedType: OptionTypeWithAll = "all"
): ValidationResult {
    if (!isObject(value)) {
        return ng("OptionsResponse は object ではありません");
    }

    if (!hasOnlyAllowedTopLevelKeys(value)) {
        return ng("OptionsResponse に許可されていないトップレベルキーがあります");
    }

    if (requestedType !== "all") {
        const onlyTarget = value[requestedType];
        if (onlyTarget == null) {
            return ng(`要求したカテゴリ ${requestedType} がレスポンスに存在しません`);
        }
    }

    for (const [key, child] of Object.entries(value)) {
        const result = validateProductOption(child, key);
        if (!result.ok) {
            return result;
        }
    }

    return ok();
}

export function isOptionsResponse(value: unknown): value is OptionsResponse {
    return validateOptionsResponse(value).ok;
}

export function assertOptionsResponse(
    value: unknown,
    requestedType: OptionTypeWithAll = "all"
): asserts value is OptionsResponse {
    const result = validateOptionsResponse(value, requestedType);

    if (!result.ok) {
        throw new Error(`OptionsResponse の型チェックに失敗しました: ${result.message}`);
    }
}

/**
 * APIを呼び出す関数
 * @param url - APIのURL
 * @param method - HTTPメソッド
 * @param data - 送信するデータ
 * @returns APIのレスポンス
 */
export async function callApi({
    url,
    method = "POST",
    data = null
}: {
    url: string;
    method?: "GET" | "POST" | "PUT" | "DELETE";
    data?: any;
}): Promise<any> {
    //API呼び出しのログをコンソールに表示
    console.info(`apiCalled: ${method} ${url} with data:`, data);
    return new Promise((resolve, reject) => {
        const xhr = new XMLHttpRequest();
        xhr.open(method, url);
        xhr.setRequestHeader("Content-Type", "application/json");
        xhr.onload = () => {
            if (xhr.status >= 200 && xhr.status < 300) {
                //結果をコンソールに表示
                console.info(`apiResponse: ${method} ${url} response:`, xhr.responseText);
                resolve(JSON.parse(xhr.responseText));
            } else {
                reject(new Error(`API call failed with status ${xhr.status}: ${xhr.statusText}`));
            }
        };
        xhr.onerror = () => reject(new Error("Network error"));
        xhr.send(data ? JSON.stringify(data) : null);
    });
}

/**
 * オプションを取得する関数
 * @param type - 取得するオプションの種類
 * @returns 取得したオプションのデータ
 */
export async function getOptions(type: OptionTypeWithAll = "all"): Promise<OptionsResponse> {
    try {
        const raw = await callApi({
            url: OPTIONS_API_URL,
            method: "POST",
            data: { action: "getOptions", type }
        });

        const parsed: unknown = typeof raw === "string" ? JSON.parse(raw) : raw;

        assertOptionsResponse(parsed, type);

        return parsed;
    } catch (error) {
        console.error("Failed to fetch options:", error);
        throw error;
    }
}

/**
 * 検索APIを呼び出して商品リストを取得します。
 *
 * @param searchText - 検索語句（空文字の場合は全件検索）
 * @param type - 検索対象のカテゴリ
 * @param options - 検索オプション
 * @param page - 1始まりのページ番号
 * @param pageSize - 1ページあたりの件数
 * @returns 商品リスト + ページ情報
 */
export async function search(
    searchText: string,
    type: OptionTypeWithAll,
    options: OptionsResponse,
    page: number = 1,
    pageSize: number = 20
): Promise<SearchResponse> {
    try {
        const safePage = Number.isFinite(page) && page > 0 ? Math.floor(page) : 1;
        const safePageSize = Number.isFinite(pageSize) && pageSize > 0 ? Math.floor(pageSize) : 20;
        const offset = (safePage - 1) * safePageSize;

        const raw = await callApi({
            url: `${API_URL}Option`,
            method: "POST",
            data: {
                action: "search",
                searchText,
                type,
                options,
                maxResults: safePageSize,
                offset
            }
        });

        if (typeof raw === "string") {
            return JSON.parse(raw);
        }
        return raw;
    } catch (error) {
        console.error("Failed to search:", error);
        throw error;
    }
}

function camelToKebab(value: string): string {
    return value.replace(/[A-Z]/g, (m) => `-${m.toLowerCase()}`);
}

function safeId(id: string): string {
    if (!id || !id.trim()) {
        throw new Error("IDが空です。");
    }
    return id.trim();
}

function appendChildToArea(areaId: string, child: HTMLElement): void {
    const area = document.getElementById(areaId);
    if (!area) {
        throw new Error(`ID:"${areaId}"を持つ要素が見つかりません。`);
    }
    area.appendChild(child);
}

export async function addArea(
    areaId: string,
    addId: string,
    classId: string,
): Promise<HTMLDivElement> {
    areaId = safeId(areaId);
    addId = safeId(addId);

    const area = document.createElement("div");
    area.id = addId;
    area.className = classId;
    appendChildToArea(areaId, area);
    return area;
}

export function addCheckbox(
    checkboxId: string,
    labelText: string,
    areaId: string,
    classId: string,
    changeFnc: (newValue: boolean) => void
): HTMLLabelElement {
    checkboxId = safeId(checkboxId);
    areaId = safeId(areaId);

    const wrapper = document.createElement("label");

    const input = document.createElement("input");
    input.type = "checkbox";
    input.id = checkboxId;
    input.className = classId;
    input.addEventListener("change", () => changeFnc(input.checked));

    const text = document.createElement("span");
    text.textContent = labelText;

    wrapper.appendChild(input);
    wrapper.appendChild(text);
    appendChildToArea(areaId, wrapper);

    return wrapper;
}

function getDecimalPlaces(value: number): number {
    if (!Number.isFinite(value)) {
        return 0;
    }

    const text = value.toString().toLowerCase();

    if (text.includes("e-")) {
        const [base, exponent] = text.split("e-");
        const decimalPartLength = (base.split(".")[1] ?? "").length;
        return decimalPartLength + Number(exponent);
    }

    return (text.split(".")[1] ?? "").length;
}

function normalizeByPrecision(value: number, precision: number): number {
    return Number(value.toFixed(precision));
}

function getIntegerSliderStep(maxAbsValue: number): number {
    const abs = Math.abs(maxAbsValue);

    if (abs >= 1_000_000) return 10_000;
    if (abs >= 100_000) return 1_000;
    if (abs >= 10_000) return 100;
    if (abs >= 1_000) return 10;
    return 1;
}

function snapToStep(
    value: number,
    step: number,
    mode: "round" | "floor" | "ceil" = "round"
): number {
    if (step <= 0) {
        return value;
    }

    const quotient = value / step;

    let snapped: number;
    switch (mode) {
        case "floor":
            snapped = Math.floor(quotient) * step;
            break;
        case "ceil":
            snapped = Math.ceil(quotient) * step;
            break;
        default:
            snapped = Math.round(quotient) * step;
            break;
    }

    return normalizeByPrecision(snapped, getDecimalPlaces(step));
}

function formatSliderValue(value: number, precision: number): string {
    if (precision <= 0) {
        return Math.round(value).toString();
    }
    return value.toFixed(precision);
}

function resolveSliderNumberConfig(
    min: number,
    max: number,
    start: number | [number, number]
): {
    precision: number;
    step: number;
    rangeMin: number;
    rangeMax: number;
    startValues: number[];
} {
    const startValues = Array.isArray(start) ? start : [start];
    const precision = Math.max(
        getDecimalPlaces(min),
        getDecimalPlaces(max),
        ...startValues.map((value) => getDecimalPlaces(value))
    );

    if (precision === 0) {
        const maxAbsValue = Math.max(
            Math.abs(min),
            Math.abs(max),
            ...startValues.map((value) => Math.abs(value))
        );

        const step = getIntegerSliderStep(maxAbsValue);

        return {
            precision: 0,
            step,
            rangeMin: snapToStep(min, step, "floor"),
            rangeMax: snapToStep(max, step, "ceil"),
            startValues: startValues.map((value) => snapToStep(value, step, "round"))
        };
    }

    const step = 1 / Math.pow(10, precision);

    return {
        precision,
        step: normalizeByPrecision(step, precision),
        rangeMin: normalizeByPrecision(min, precision),
        rangeMax: normalizeByPrecision(max, precision),
        startValues: startValues.map((value) => normalizeByPrecision(value, precision))
    };
}

function createNoUiSlider(container: HTMLElement, options: any) {
    const sliderFactory = (window as any).noUiSlider;

    if (!sliderFactory || typeof sliderFactory.create !== "function") {
        throw new Error("noUiSlider.create が見つかりませんでした。html で nouislider の script を読み込んでいるか確認してください。");
    }

    sliderFactory.create(container, options);
    return (container as any).noUiSlider;
}

function setNoUiSliderTooltipVisible(container: HTMLElement, visible: boolean): void {
    const tooltips = container.querySelectorAll(".noUi-tooltip");

    tooltips.forEach((tooltip) => {
        if (!(tooltip instanceof HTMLElement)) {
            return;
        }

        tooltip.style.opacity = visible ? "1" : "0";
        tooltip.style.visibility = visible ? "visible" : "hidden";
        tooltip.style.pointerEvents = "none";
        tooltip.style.transition = "opacity 0.12s ease";
    });
}

export async function addNoUiSlider(
    containerId: string,
    min: number,
    max: number,
    areaId: string,
    classId: string,
    changeFnc: (minValue: number, maxValue: number) => void
): Promise<unknown> {
    containerId = safeId(containerId);
    areaId = safeId(areaId);

    const container = document.createElement("div");
    container.id = containerId;
    container.className = classId;
    appendChildToArea(areaId, container);

    const sliderConfig = resolveSliderNumberConfig(min, max, [min, max]);

    const options = {
        start: sliderConfig.startValues,
        connect: true,
        range: {
            min: sliderConfig.rangeMin,
            max: sliderConfig.rangeMax
        },
        step: sliderConfig.step,
        tooltips: true,
        format: {
            to: (value: number) => formatSliderValue(value, sliderConfig.precision),
            from: (value: string) => parseFloat(value)
        }
    };

    const slider = createNoUiSlider(container, options);

    setNoUiSliderTooltipVisible(container, false);

    if (typeof slider.on !== "function") {
        console.warn("noUiSlider の戻り値に .on がありません。イベント登録をスキップします。", slider);
        return slider;
    }

    slider.on("start", () => {
        setNoUiSliderTooltipVisible(container, true);
    });

    slider.on("end", () => {
        setNoUiSliderTooltipVisible(container, false);
    });

    slider.on("change", (values: any[]) => {
        const minValueRaw = typeof values[0] === "string" ? parseFloat(values[0]) : values[0];
        const maxValueRaw = typeof values[1] === "string" ? parseFloat(values[1]) : values[1];

        const minValue = normalizeByPrecision(minValueRaw, sliderConfig.precision);
        const maxValue = normalizeByPrecision(maxValueRaw, sliderConfig.precision);

        changeFnc(minValue, maxValue);
    });

    return slider;
}

export async function addNoUiSliderSingle(
    containerId: string,
    value: number,
    min: number,
    max: number,
    areaId: string,
    classId: string,
    changeFnc: (value: number) => void
) {
    containerId = safeId(containerId);
    areaId = safeId(areaId);

    const container = document.createElement("div");
    container.id = containerId;
    container.className = classId;
    appendChildToArea(areaId, container);

    const sliderConfig = resolveSliderNumberConfig(min, max, value);

    const options = {
        start: sliderConfig.startValues[0],
        connect: [true, false],
        range: {
            min: sliderConfig.rangeMin,
            max: sliderConfig.rangeMax
        },
        step: sliderConfig.step,
        tooltips: true,
        format: {
            to: (value: number) => formatSliderValue(value, sliderConfig.precision),
            from: (value: string) => parseFloat(value)
        }
    };

    const slider = createNoUiSlider(container, options);

    setNoUiSliderTooltipVisible(container, false);

    if (typeof slider.on !== "function") {
        console.warn("noUiSlider の戻り値に .on がありません。イベント登録をスキップします。", slider);
        return slider;
    }

    slider.on("start", () => {
        setNoUiSliderTooltipVisible(container, true);
    });

    slider.on("end", () => {
        setNoUiSliderTooltipVisible(container, false);
    });

    slider.on("change", (values: any[]) => {
        const rawValue = typeof values[0] === "string" ? parseFloat(values[0]) : values[0];
        const normalizedValue = normalizeByPrecision(rawValue, sliderConfig.precision);
        changeFnc(normalizedValue);
    });

    return slider;
}

function getContextPath(): string {
    const pathName = window.location.pathname;
    const segments = pathName.split("/").filter(Boolean);

    if (segments.length === 0) {
        return "";
    }

    return `/${segments[0]}`;
}

function buildProductDebugUrl(productId: number): string {
    const contextPath = getContextPath();
    const url = new URL(`${contextPath}/servlet/ProductServlet`, window.location.origin);
    url.searchParams.set("productId", String(productId));
    url.searchParams.set("debug", "true");
    return url.toString();
}

function formatProductPrice(price: number): string {
    return `￥${price.toLocaleString()}`;
}

function resolveProductImageUrl(product: Product): string {
    if (!product.imageId) {
        return "data:image/svg+xml;charset=UTF-8," + encodeURIComponent(`
            <svg xmlns="http://www.w3.org/2000/svg" width="320" height="240" viewBox="0 0 320 240">
                <rect width="320" height="240" fill="#f3f4f6"/>
                <text x="50%" y="50%" dominant-baseline="middle" text-anchor="middle" fill="#9ca3af" font-size="20" font-family="sans-serif">NO IMAGE</text>
            </svg>
        `);
    }

    if (/^(https?:)?\/\//.test(product.imageId) || product.imageId.startsWith("/") || product.imageId.startsWith(".")) {
        return product.imageId;
    }

    return product.imageId;
}

function buildProductSpecText(product: Product): string | null {
    switch (product.productType) {
        case "cpu":
            return `${product.generation} / ${product.core}C ${product.thread}T / ${product.clock}GHz`;
        case "gpu":
            return `${product.seriesName} / ${product.chipName} / ${product.vram}GB`;
        case "memory":
            return `${product.generation} / ${product.capacity}`;
        case "mother_board":
            return `${product.chipset} / ${product.size}`;
        case "ssd":
            return `${product.capacity} / ${product.type}`;
        case "hdd":
            return `${product.capacity} / ${product.rpm}`;
        default:
            return null;
    }
}

function createMetaText(text: string, classId: string): HTMLDivElement {
    const element = document.createElement("div");
    element.textContent = text;
    element.className = classId;
    return element;
}

/**
 * 商品用のカード要素を生成して指定エリアに追加します。
 */
export function addProductCard(
    product: Product,
    areaId: string,
    classId: Record<keyof typeof classIds.productCard, string>
): HTMLDivElement {
    areaId = safeId(areaId);

    const card = document.createElement("div");
    card.className = classId.base;
    card.tabIndex = 0;
    card.setAttribute("role", "link");
    card.setAttribute("aria-label", `${product.name} の詳細を開く`);

    const openDetail = (): void => {
        window.location.href = buildProductDebugUrl(product.productId);
    };

    card.addEventListener("click", openDetail);
    card.addEventListener("keydown", (event: KeyboardEvent) => {
        if (event.key === "Enter" || event.key === " ") {
            event.preventDefault();
            openDetail();
        }
    });

    const imageWrap = document.createElement("div");
    imageWrap.className = `${classId.imageWrap}`;

    const image = document.createElement("img");
    image.src = resolveProductImageUrl(product);
    image.alt = product.name;
    image.className = `${classId.image}`;
    image.addEventListener("error", () => {
        image.src = resolveProductImageUrl({ ...product, imageId: null });
    });
    imageWrap.appendChild(image);

    const makerText = createMetaText(product.makerName, `${classId.maker}`);

    const nameElem = document.createElement("div");
    nameElem.textContent = product.name;
    nameElem.className = `${classId.name}`;

    const specText = buildProductSpecText(product);
    const specElem = createMetaText(specText ?? `在庫: ${product.stock}`, `${classId.spec}`);

    const priceBlock = document.createElement("div");
    priceBlock.className = `${classId.priceBlock}`;

    const priceElem = document.createElement("span");
    priceElem.textContent = formatProductPrice(product.price);
    priceElem.className = `${classId.price}`;

    const taxElem = document.createElement("span");
    taxElem.textContent = " 税込";
    taxElem.className = `${classId.tax}`;

    priceBlock.appendChild(priceElem);
    priceBlock.appendChild(taxElem);

    const detailHint = createMetaText("クリックして詳細を見る", `${classId.detailHint}`);

    card.appendChild(imageWrap);
    card.appendChild(makerText);
    card.appendChild(nameElem);
    card.appendChild(specElem);
    card.appendChild(priceBlock);
    card.appendChild(detailHint);

    appendChildToArea(areaId, card);

    return card;
}

/**
 * 画像が見つからないエラーを握りつぶすための初期化関数
 * ページ内の全てのimg要素にerrorイベントリスナーを追加し、エラーを無視します。
 */
export function initImageErrorHandlers(): void {
    const images = document.querySelectorAll('img');
    images.forEach(img => {
        img.addEventListener('error', (event) => {
            // エラーを握りつぶす（コンソールにログを出力し、何もしない）
            // console.log('Image load error suppressed for:', (event.target as HTMLImageElement).src);
            // 必要に応じて img.style.display = 'none'; を追加して非表示にすることも可能
        });
    });
}