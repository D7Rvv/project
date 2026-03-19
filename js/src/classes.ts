import { OptionType, Product } from "./types";
import {
    addCheckbox,
    addNoUiSlider,
    addNoUiSliderSingle
} from "./helper";

export abstract class ProductClass {
    productId: number;
    name: string;
    maker: string;
    price: number;
    imageId: string;

    constructor(
        productId: number,
        name: string,
        maker: string,
        price: number,
        imageId: string,
    ) {
        this.productId = productId;
        this.name = name;
        this.maker = maker;
        this.price = price;
        this.imageId = imageId;
    }
}

export class CpuProduct extends ProductClass {
    generation: string;
    core: number;
    thread: number;
    clock: number;

    constructor(
        productId: number,
        name: string,
        maker: string,
        price: number,
        imageId: string,
        generation: string,
        core: number,
        thread: number,
        clock: number
    ) {
        super(productId, name, maker, price, imageId);
        this.generation = generation;
        this.core = core;
        this.thread = thread;
        this.clock = clock;
    }
}

export class GpuProduct extends ProductClass {
    series: string;
    chip: string;
    vram: number;

    constructor(
        productId: number,
        name: string,
        maker: string,
        price: number,
        imageId: string,
        series: string,
        chip: string,
        vram: number
    ) {
        super(productId, name, maker, price, imageId);
        this.series = series;
        this.chip = chip;
        this.vram = vram;
    }
}

export class MemoryProduct extends ProductClass {
    generation: string;
    capacity: string;

    constructor(
        productId: number,
        name: string,
        maker: string,
        price: number,
        imageId: string,
        generation: string,
        capacity: string
    ) {
        super(productId, name, maker, price, imageId);
        this.generation = generation;
        this.capacity = capacity;
    }
}

export class MotherBoardProduct extends ProductClass {
    chipset: string;
    size: string;

    constructor(
        productId: number,
        name: string,
        maker: string,
        price: number,
        imageId: string,
        chipset: string,
        size: string
    ) {
        super(productId, name, maker, price, imageId);
        this.chipset = chipset;
        this.size = size;
    }
}

export class SsdProduct extends ProductClass {
    capacity: string;
    type: string;

    constructor(
        productId: number,
        name: string,
        maker: string,
        price: number,
        imageId: string,
        capacity: string,
        type: string
    ) {
        super(productId, name, maker, price, imageId);
        this.capacity = capacity;
        this.type = type;
    }
}

export class HddProduct extends ProductClass {
    capacity: string;
    rpm: string;

    constructor(
        productId: number,
        name: string,
        maker: string,
        price: number,
        imageId: string,
        capacity: string,
        rpm: string
    ) {
        super(productId, name, maker, price, imageId);
        this.capacity = capacity;
        this.rpm = rpm;
    }
}

export function createProduct(data: Product): ProductClass {
    switch (data.productType) {
        case "cpu":
            return new CpuProduct(data.productId, data.name, data.makerName, data.price, data.imageId || "", data.generation, data.core, data.thread, data.clock);
        case "gpu":
            return new GpuProduct(data.productId, data.name, data.makerName, data.price, data.imageId || "", data.seriesName, data.chipName, data.vram);
        case "memory":
            return new MemoryProduct(data.productId, data.name, data.makerName, data.price, data.imageId || "", data.generation, data.capacity);
        case "mother_board":
            return new MotherBoardProduct(data.productId, data.name, data.makerName, data.price, data.imageId || "", data.chipset, data.size);
        case "ssd":
            return new SsdProduct(data.productId, data.name, data.makerName, data.price, data.imageId || "", data.capacity, data.type);
        case "hdd":
            return new HddProduct(data.productId, data.name, data.makerName, data.price, data.imageId || "", data.capacity, data.rpm);
    }
}

export abstract class BaseOption {
    abstract optionType: string;
    name: string;

    constructor(name: string) {
        this.name = name;
    }
}

export class SelectableOption extends BaseOption {
    optionType: string = "selectable";
    value: string;
    isSelected: boolean;

    constructor(name: string, value: string, isSelected: boolean) {
        super(name);
        this.value = value;
        this.isSelected = isSelected;
    }
}

export class RangeOption extends BaseOption {
    optionType: string = "range";
    minValue: number;
    maxValue: number;
    maxLimit: number;
    minLimit: number;

    constructor(name: string, maxValue: number, minValue: number, maxLimit: number, minLimit: number) {
        super(name);
        this.maxValue = maxValue;
        this.minValue = minValue;
        this.maxLimit = maxLimit;
        this.minLimit = minLimit;
    }
}


export class NumberOption extends RangeOption {
    optionType: string = "number";

    constructor(name: string, maxLimit: number, minLimit: number, value: number) {
        super(name, value, value, maxLimit, minLimit);
    }
}

export abstract class ProductOptionClass {
    abstract optionType: OptionType;
    maker: SelectableOption[];
    price: RangeOption;
    constructor(maker: SelectableOption[], price: RangeOption) {
        this.maker = maker;
        this.price = price;
    }
}

export class CpuOptionClass extends ProductOptionClass {
    optionType: OptionType = "cpu";
    generation: SelectableOption[];
    core: RangeOption;
    thread: RangeOption;
    clock: NumberOption;

    constructor(
        maker: SelectableOption[],
        price: RangeOption,
        generation: SelectableOption[],
        core: RangeOption,
        thread: RangeOption,
        clock: NumberOption
    ) {
        super(maker, price);
        this.generation = generation;
        this.core = core;
        this.thread = thread;
        this.clock = clock;
    }
}