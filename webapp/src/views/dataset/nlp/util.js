/* eslint-disable */

// 参考来源：https://github.com/alienzhou/web-highlighter/blob/1b26fe5927/src/util/uuid.ts
export function createUUID(a) {
  return a
    ? (a ^ ((Math.random() * 16) >> (a / 4))).toString(16)
    : ([1e7] + -1e3 + -4e3 + -8e3 + -1e11).replace(/[018]/gu, createUUID);
}

// 将原始值（highlightSource）转化为 json
// [{ startMeta: {parentIndex: 0, parentTagName: "PRE", textOffset: 64 }, endMeta: { parentIndex: 0, parentTagName: "PRE", textOffset: 82 }, id: 'xxx', text: 'text', __isHighlightSource: {} }]
export const hs2Json = raw => {
  return raw.map(d => {
    const result = {
      offset: [d.startMeta.textOffset, d.endMeta.textOffset],
      text: d.text,
    };
    if (d.extra && d.extra.labelId) {
      Object.assign(result, {
        labelId: d.extra.labelId, // 对应的标签
      });
    }
    return result;
  });
};

export const json2Hs = arr => {
  return arr.map(d => {
    const res = {
      startMeta: { parentIndex: 0, parentTagName: 'PRE', textOffset: d.offset[0] },
      endMeta: { parentIndex: 0, parentTagName: 'PRE', textOffset: d.offset[1] },
      id: createUUID(),
      text: d.text,
      __isHighlightSource: {},
    };
    if (d.labelId) {
      res.extra = {
        labelId: d.labelId,
        type: 'from-store',
      };
    }
    return res;
  });
};
