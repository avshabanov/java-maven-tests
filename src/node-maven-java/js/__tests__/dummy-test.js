//import smth from '../util/something-else.js';

describe('tests dummy operation', function () {
  it('adds one to empty', function () {
    // Given:
    const empty = "";

    // When:
    const one = "1";

    // Then:
    expect(empty + one).toBe("1");
  });
});
