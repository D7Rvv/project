// search_test.js
//  - ここでは API 呼び出しに関する最低限の機能のみを実装します。
//  - それ以外（レンダリング・フォーム連携など）は XXXX を実装予定、とします。

const ID_SEARCH_FORM = 'searchForm';
const ID_INPUT_VALUE = 'value';
const ID_SELECT_TYPE = 'type';
const ID_INPUT_MAX_RESULTS = 'maxResults';
const ID_RESULT_TABLE_BODY = 'resultTableBody';
const ID_RESULT_SUMMARY = 'resultSummary';
const ID_RESULT_KEYWORD = 'resultKeyword';
const ID_RESULT_COUNT = 'resultCount';
const ID_NO_RESULTS = 'noResults';
const ID_NO_RESULTS_KEYWORD = 'noResultsKeyword';

const API_URL = `${location.origin}${location.pathname.replace(/\/[^/]*$/, '')}/Servlet/API/Option`;

/**
 * API を呼び出す
 * @param {string} action
 * @param {object} body
 * @returns {Promise<any>}
 */
export async function callApi(action, body) {
  const payload = body || {};
  payload.action = action;

  const resp = await fetch(API_URL, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });

  if (!resp.ok) {
    throw new Error(`HTTP ${resp.status} ${resp.statusText}`);
  }

  const json = await resp.json();
  if (typeof json === 'string') {
    try {
      return JSON.parse(json);
    } catch {
      return json;
    }
  }

  return json;
}

// TODO: レンダリング処理をここで実装予定
// function renderResults(...) { ... }

// TODO: フォーム連携（入力から API 呼び出し）をここで実装予定
// function initPage() { ... }

// ページ初期化
// TODO: 実装予定
// initPage();
