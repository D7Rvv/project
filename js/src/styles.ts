export type StyleMap = {
    [key: string]: string;
};

export const styles = {
    example: {
        padding: "12px 20px",
        border: "none",
        borderRadius: "999px",
        background: "linear-gradient(135deg, #4f8cff, #2563eb)",
        color: "#ffffff",
        fontSize: "14px",
        fontWeight: "700",
        cursor: "pointer",
        boxShadow: "0 4px 10px rgba(37, 99, 235, 0.25)",
        transition: "transform 0.15s ease, box-shadow 0.15s ease, opacity 0.15s ease"
    },

    // オプショングループのスタイル
    optionGroup: {
        padding: "14px 14px 12px",
        border: "1px solid #e2e8f0",
        borderRadius: "14px",
        backgroundColor: "#ffffff"
    },

    // オプションセクションのラベルスタイル
    optionSectionLabel: {
        fontSize: "13px",
        fontWeight: "800",
        letterSpacing: "0.04em",
        color: "#1d4ed8",
        margin: "0 0 10px"
    },

    // オプションのタイトルスタイル
    optionTitle: {
        fontSize: "15px",
        fontWeight: "700",
        margin: "12px 0 8px",
        color: "#0f172a"
    },

    // チェックボックスのスタイル
    checkbox: {
        display: "flex",
        alignItems: "center",
        gap: "8px",
        margin: "6px 0",
        fontSize: "14px",
        color: "#334155"
    },
    
    // ダブルスライダーのスタイル
    // NoUiSliderを使用
    doubleSlider: {
        width: "100%",
        margin: "12px 0 6px"
    },

    // 商品カードのスタイル
    productCard: {
        display: "flex",
        flexDirection: "column",
        alignItems: "stretch",
        padding: "16px",
        border: "1px solid #e5e7eb",
        borderRadius: "12px",
        backgroundColor: "#ffffff",
        boxShadow: "0 1px 4px rgba(15, 23, 42, 0.08)",
        transition: "transform 0.2s ease, box-shadow 0.2s ease",
        cursor: "pointer"
    },

    // 商品カード内の画像ラッパーのスタイル
    productCardImageWrap: {
        width: "100%",
        height: "220px",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        marginBottom: "12px",
        backgroundColor: "#ffffff"
    },

    // 商品カード内の画像のスタイル
    productCardImage: {
        maxWidth: "100%",
        maxHeight: "100%",
        objectFit: "contain"
    },

    // 商品カード内のメーカー名のスタイル
    productCardMaker: {
        fontSize: "12px",
        color: "#565959",
        lineHeight: "1.4",
        marginBottom: "4px",
        width: "100%"
    },

    // 商品カード内の商品名のスタイル
    productCardName: {
        fontSize: "16px",
        lineHeight: "1.4",
        color: "#2162a1",
        fontWeight: "500",
        width: "100%",
        minHeight: "44px",
        marginBottom: "6px",
        display: "-webkit-box",
        WebkitLineClamp: "2",
        WebkitBoxOrient: "vertical",
        overflow: "hidden"
    },

    // 商品カード内のスペックテキストのスタイル
    productCardSpec: {
        fontSize: "12px",
        color: "#565959",
        lineHeight: "1.45",
        marginBottom: "8px",
        width: "100%",
        minHeight: "34px"
    },

    // 商品カード内の価格ブロックのスタイル
    productCardPriceBlock: {
        width: "100%",
        marginBottom: "10px",
        color: "#0f1111"
    },

    // 商品カード内の価格のスタイル
    productCardPrice: {
        fontSize: "24px",
        fontWeight: "500",
        lineHeight: "1.2"
    },

    // 商品カード内の税込表示のスタイル
    productCardTax: {
        fontSize: "12px",
        color: "#565959",
        marginLeft: "4px"
    },

    // 商品カード内の詳細ヒントのスタイル
    productCardDetailHint: {
        fontSize: "12px",
        color: "#2162a1",
        marginTop: "auto"
    },

    // ページ移動ボタンのスタイル
    pageButton: {
        padding: "10px 16px",
        border: "1px solid #cbd5e1",
        borderRadius: "999px",
        backgroundColor: "#ffffff",
        color: "#334155",
        cursor: "pointer",
        fontWeight: "700"
    },
    
    // 現在のページを示すスタイル
    currentPage: {
        padding: "10px 16px",
        border: "1px solid #2563eb",
        borderRadius: "999px",
        backgroundColor: "#2563eb",
        color: "#ffffff",
        fontWeight: "700"
    }
} satisfies Record<string, StyleMap>;
