export function parseUrlParams(url: string) {
  const params = url
    .split('?')?.[1]
    .split('&')
    .reduce(
      (obj, el: string) => {
        const tmp = el.split('=');
        if (tmp?.length > 0) {
          return { ...obj, [tmp[0]]: tmp?.[1] };
        } else {
          return obj;
        }
      },
      { aesKey: null, issuer: null, secret: null, sessionId: null, data: null }
    );
  return params;
}
