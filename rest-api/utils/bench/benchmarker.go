package bench

import (
	"fmt"
	"time"
)

type Bench struct {
	start   time.Time
	records []string
}

func New() *Bench {
	return &Bench{start: time.Now()}
}

func (b *Bench) Step(name string) {
	elapsed := time.Since(b.start)
	b.records = append(b.records, fmt.Sprintf("%s: %v", name, elapsed))
	b.start = time.Now()
}

func (b *Bench) Report() {
	fmt.Printf("Benchmark results:\n%s",
		func() string {
			out := ""
			for _, r := range b.records {
				out += "\t" + r + "\n"
			}
			return out
		}(),
	)
}
