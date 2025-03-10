const ITERATION_BUFFER = Array(1).fill(-1);

const output = {};

class TimeSeries {
    #iterationNumberBuffer = ITERATION_BUFFER;
    #dataBuffer
    #length

    get dataBuffer() {
        return this.#dataBuffer;
    }

    constructor(size, dataBuffer = Array(size).fill(NaN)) {
        this.#dataBuffer = dataBuffer
        this.#length = size
    }

    last(idx = 0) {
        if (idx > this.#iterationNumberBuffer[0]) {
            return NaN;
        }
        let shiftedIdx = (this.#iterationNumberBuffer[0] - idx) % this.#length
        return this.#dataBuffer[shiftedIdx]
    }
}

function moving_average(data, num_periods) {
    if (ITERATION_BUFFER[0] < num_periods - 1) {
        return NaN
    }
    let v = 0.0
    let k = 0
    for (let i = 0; i < num_periods; ++i) {
        let u = data.last(i)
        v += u
        k += 1
    }
    return v / k
}