// API 用の型定義

export type OptionType = "cpu" | "gpu" | "memory" | "mother_board" | "ssd" | "hdd";
export type OptionTypeWithAll = OptionType | "all";

export type SelectableOption = {
    name: string;
    value: string;
    isSelected: boolean;
};

export type RangeOption = {
    name: string;
    minValue: number;
    maxValue: number;
    maxLimit: number;
    minLimit: number;
};

export type CpuOption = {
    optionType: "CPU";
    maker: SelectableOption[];
    price: RangeOption;
    generation: SelectableOption[];
    core: RangeOption;
    thread: RangeOption;
    clock: RangeOption;
};

export type GpuOption = {
    optionType: "GPU";
    maker: SelectableOption[];
    price: RangeOption;
    series: SelectableOption[];
    chip: SelectableOption[];
    vram: RangeOption;
};

export type MemoryOption = {
    optionType: "MEMORY";
    maker: SelectableOption[];
    price: RangeOption;
    generation: SelectableOption[];
    capacity: SelectableOption[];
};

export type MotherBoardOption = {
    optionType: "MOTHER_BOARD";
    maker: SelectableOption[];
    price: RangeOption;
    chipset: SelectableOption[];
    size: SelectableOption[];
};

export type SsdOption = {
    optionType: "SSD";
    maker: SelectableOption[];
    price: RangeOption;
    capacity: SelectableOption[];
    type: SelectableOption[];
};

export type HddOption = {
    optionType: "HDD";
    maker: SelectableOption[];
    price: RangeOption;
    capacity: SelectableOption[];
    rpm: SelectableOption[];
};

export type ProductOptionResponse =
    | CpuOption
    | GpuOption
    | MemoryOption
    | MotherBoardOption
    | SsdOption
    | HddOption;

export type OptionsResponse = Partial<{
    cpu: CpuOption;
    gpu: GpuOption;
    memory: MemoryOption;
    mother_board: MotherBoardOption;
    ssd: SsdOption;
    hdd: HddOption;
}>;

/**
 * API の検索結果として返ってくる商品情報の共通フィールド。
 */
export type ProductBase = {
    productId: number;
    name: string;
    price: number;
    stock: number;
    imageId?: string | null;
    makerId: number;
    makerName: string;
    productType?: string | null;
};

export type CpuProduct = ProductBase & {
    productType: "cpu";
    cpuId: number;
    generation: string;
    core: number;
    thread: number;
    clock: number;
};

export type GpuProduct = ProductBase & {
    productType: "gpu";
    gpuId: number;
    seriesName: string;
    chipName: string;
    vram: number;
};

export type MemoryProduct = ProductBase & {
    productType: "memory";
    memoryId: number;
    generation: string;
    capacity: string;
};

export type MotherBoardProduct = ProductBase & {
    productType: "mother_board";
    motherBoardId: number;
    chipset: string;
    size: string;
};

export type SsdProduct = ProductBase & {
    productType: "ssd";
    ssdId: number;
    capacity: string;
    type: string;
};

export type HddProduct = ProductBase & {
    productType: "hdd";
    hddId: number;
    capacity: string;
    rpm: string;
};

export type Product =
    | CpuProduct
    | GpuProduct
    | MemoryProduct
    | MotherBoardProduct
    | SsdProduct
    | HddProduct;

/**
 * search API のレスポンス型（商品リスト）
 */
export type SearchResponse = {
    products: Product[];
    info?:{
        offset?: number;
        resultCount?: number;
        totalCount?: number;
        resultPage?: number;
        totalPage?: number;
    }
};

export type NoUiSliderInstance = {
    destroy(): void;
    get(): number | string | (number | string)[];
    set(value: number | string | (number | string)[]): void;
    on(event: string, callback: (...args: unknown[]) => void): void;
    updateOptions(options: {
        range?: {
            min: number;
            max: number;
        };
        step?: number;
        start?: number | number[];
    }): void;
};

export type CommonMakerItem = {
    category: OptionType;
    option: SelectableOption;
    label: string;
    key: string;
};

export type CommonRenderState = {
    makers: CommonMakerItem[];
    price: RangeOption | null;
};