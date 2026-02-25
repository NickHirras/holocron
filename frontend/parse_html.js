const fs = require('fs');

const content = fs.readFileSync('src/app/ceremony-creator/ceremony-creator.html', 'utf8');
const lines = content.split('\n');

let openTags = [];
let openBlocks = [];

for (let i = 0; i < lines.length; i++) {
    const line = lines[i];
    
    // Check for @if, @for
    if (line.match(/@(if|for)\s*\(/)) {
        openBlocks.push({ type: 'block', line: i + 1 });
    }
    
    // Check for }
    if (line.match(/\}/)) {
        if (openBlocks.length > 0) {
            openBlocks.pop();
        } else {
            console.log(`Unmatched } at line ${i + 1}`);
        }
    }
    
    // Simplified tag matching (just counting div and form for now, ignoring self-closing)
    let m;
    const divOpenRe = /<div[^>]*>/g;
    while ((m = divOpenRe.exec(line)) !== null) { openTags.push({tag: 'div', line: i+1}); }
    
    const divCloseRe = /<\/div>/g;
    while ((m = divCloseRe.exec(line)) !== null) { 
        if (openTags.length > 0 && openTags[openTags.length-1].tag === 'div') {
            openTags.pop();
        } else {
            console.log(`Unmatched </div> at line ${i+1}`);
        }
    }
    
    const formOpenRe = /<form[^>]*>/g;
    while ((m = formOpenRe.exec(line)) !== null) { openTags.push({tag: 'form', line: i+1}); }
    
    const formCloseRe = /<\/form>/g;
    while ((m = formCloseRe.exec(line)) !== null) { 
        if (openTags.length > 0 && openTags[openTags.length-1].tag === 'form') {
            openTags.pop();
        } else {
             console.log(`Unmatched </form> at line ${i+1}. Last open tag was ${openTags.length > 0 ? openTags[openTags.length-1].tag : 'none'}`);
        }
    }
}

console.log('Remaining open blocks:', openBlocks);
console.log('Remaining open tags:', openTags);
