/*
 *	Z80.java
 *
 *	Copyright 2004-2010 Jan Bobrowski <jb@wizard.ae.krakow.pl>
 *
 *	This program is free software; you can redistribute it and/or
 *	modify it under the terms of the GNU General Public License
 *	version 2 as published by the Free Software Foundation.
 */

/*
 *	Based on "The Undocumented Z80 Documented" by Sean Young
 */
package org.wandora.application.gui.previews.formats.applicationz80;



public final class Z80 {

	interface Env {
		int m1(int pc, int ir);
		int mem(int addr);
		void mem(int addr, int v);
		int in(int port);
		void out(int port, int v);

		int mem16(int addr);
		void mem16(int addr, int v);

		int halt(int n, int ir);
	}

	private final Env env;

	Z80(Env env) {
		this.env = env;
	}

	private int PC, SP;
	private int A, B, C, D, E, HL;
	private int A_, B_, C_, D_, E_, HL_;
	private int IX, IY;
	private int IR, R;
	private int MP; /* MEMPTR, the hidden register
			   emulated according to memptr_eng.txt */
	private int Ff, Fr, Fa, Fb;
	private int Ff_, Fr_, Fa_, Fb_;

/*
 lazy flag evaluation:

 state is stored in four variables: Ff, Fr, Fa, Fb

   Z: Fr==0
   P: parity of Fr&0xFF
   V: (Fa.7^Fr.7) & (Fb.7^Fr.7)
   X: Fa.8
 P/V: X ? P : V
   N: Fb.9
   H: Fr.4 ^ Fa.4 ^ Fb.4 ^ Fb.12

		FEDCBA98 76543210
	Ff	.......C S.5.3...
	Fr	........ V..H....
	Fa	.......X V..H....
	Fb	...H..N. V..H....
*/
	static final int FC = 0x01;
	static final int FN = 0x02;
	static final int FP = 0x04;
	static final int F3 = 0x08;
	static final int FH = 0x10;
	static final int F5 = 0x20;
	static final int FZ = 0x40;
	static final int FS = 0x80;
	static final int F53 = 0x28;

	private int flags() {
		int f = Ff, a = Fa, b = Fb, r = Fr;
		f = f&(FS|F53) | f>>>8&FC;		// S.5.3..C
		int u = b >> 8;
		if(r == 0) f |= FZ;			// .Z......
		int ra = r ^ a;
		f |= u & FN;				// ......N.
		f |= (ra ^ b ^ u) & FH;			// ...H....
		if((a&~0xFF)==0) {
			a = ra & (b ^ r);
			b = 5;				// .....V..
		} else {
			a = 0x9669*FP;
			b = (r ^ r>>>4)&0xF;		// .....P..
		}
		return f | a>>>b & FP;
	}

	private void flags(int f) {
		Fr = ~f&FZ;
		Ff = (f |= f<<8);
		Fa = 0xFF & (Fb = f&~0x80 | (f&FP)<<5);
	}

	int a() {return A;}
	int f() {return flags();}
	int bc() {return B<<8|C;}
	int de() {return D<<8|E;}
	int hl() {return HL;}
	int ix() {return IX;}
	int iy() {return IY;}
	int pc() {return PC;}
	int sp() {return SP;}
	int af() {return A<<8 | flags();}

	int i() {return IR>>>8;}
	int r() {return R&0x7F | IR&0x80;}
	int im() {int v=IM; return v==0?v:v-1;}
	int iff() {return IFF;}
	boolean ei() {return (IFF&1)!=0;}

	void a(int v) {A = v;}
	void f(int v) {flags(v);}
	void bc(int v) {C=v&0xFF; B=v>>>8;}
	void de(int v) {E=v&0xFF; D=v>>>8;}
	void hl(int v) {HL = v;}
	void ix(int v) {IX = v;}
	void iy(int v) {IY = v;}
	void pc(int v) {PC = v;}
	void sp(int v) {SP = v;}
	void af(int v) {A = v>>>8; flags(v&0xFF);}

	void i(int v) {IR = IR&0xFF | v<<8;}
	void r(int v) {R=v; IR = IR&0xFF00 | v&0x80;}
	void im(int v) {IM = v+1 & 3;}
	void iff(int v) {IFF = v;}
	void ei(boolean v) {IFF = v ? 3 : 0;}

	int time;
	int time_limit;

	void exx() {
		int tmp;
		tmp = B_; B_ = B; B = tmp;
		tmp = C_; C_ = C; C = tmp;
		tmp = D_; D_ = D; D = tmp;
		tmp = E_; E_ = E; E = tmp;
		tmp = HL_; HL_ = HL; HL = tmp;
	}

	void ex_af() {
		int tmp = A_; A_ = A; A = tmp;
		tmp = Ff_; Ff_ = Ff; Ff = tmp;
		tmp = Fr_; Fr_ = Fr; Fr = tmp;
		tmp = Fa_; Fa_ = Fa; Fa = tmp;
		tmp = Fb_; Fb_ = Fb; Fb = tmp;
	}

	public void push(int v) {
		int sp;
		time++;
		env.mem((char)((sp=SP)-1), v>>>8);
		time += 3;
		env.mem(SP = (char)(sp-2), v&0xFF);
		time += 3;
	}

	int pop() {
		int sp, v = env.mem16(sp=SP);
		SP = (char)(sp+2);
		time += 6;
		return v;
	}

	private void add(int b)
	{
		A = Fr = (Ff = (Fa = A) + (Fb = b)) & 0xFF;
	}

	private void adc(int b)
	{
		A = Fr = (Ff = (Fa = A) + (Fb = b) + (Ff>>>8 & FC)) & 0xFF;
	}

	private void sub(int b)
	{
		Fb = ~b;
		A = Fr = (Ff = (Fa = A) - b) & 0xFF;
	}

	private void sbc(int b)
	{
		Fb = ~b;
		A = Fr = (Ff = (Fa = A) - b - (Ff>>>8 & FC)) & 0xFF;
	}

	private void cp(int b)
	{
		int r = (Fa = A) - b;
		Fb = ~b;
		Ff = r&~F53 | b&F53;
		Fr = r&0xFF;
	}

	private void and(int b) {Fa = ~(A = Ff = Fr = A & b); Fb = 0;}
	private void or(int b) {Fa = (A = Ff = Fr = A | b) | 0x100; Fb = 0;}
	private void xor(int b) {Fa = (A = Ff = Fr = A ^ b) | 0x100; Fb = 0;}

	private void cpl()
	{
		Ff = Ff&~F53 | (A ^= 0xFF)&F53;
		Fb |= ~0x80; Fa = Fa&~FH | ~Fr&FH; // set H, N
	}

	private int inc(int v)
	{
		Ff = Ff&0x100 | (Fr = v = (Fa=v)+(Fb=1) & 0xFF);
		return v;
	}

	private int dec(int v)
	{
		Ff = Ff&0x100 | (Fr = v = (Fa=v)+(Fb=-1) & 0xFF);
		return v;
	}

	private void bit(int n, int v)
	{
		int m = v & 1<<n;
		Ff = Ff&~0xFF | v&F53 | m;
		Fa = ~(Fr = m); Fb = 0;
	}

	private void f_szh0n0p(int r)
	{
		// SZ5H3PNC
		// xxx0xP0.
		Ff = Ff&~0xFF | (Fr = r);
		Fa = r|0x100; Fb = 0;
	}

	private void rot(int a)
	{
		Ff = Ff&0xD7 | a&0x128;
		Fb &= 0x80; Fa = Fa&~FH | Fr&FH; // reset H, N
		A = a&0xFF;
	}

	private int shifter(int o, int v)
	{
		switch(o&7) {
		case 0: v = v*0x101>>>7; break;
		case 1: v = v*0x80800000>>24; break;
		case 2: v = v<<1|Ff>>>8&1; break;
		case 3: v = (v*0x201|Ff&0x100)>>>1; break;
		case 4: v <<= 1; break;
		case 5: v = v>>>1|v&0x80|v<<8; break;
		case 6: v = v<<1|1; break;
		case 7: v = v*0x201>>>1; break;
		}
		Fa = 0x100 | (Fr = v = 0xFF&(Ff = v));
		Fb = 0;
		return v;
	}

	private int add16(int a, int b)
	{
		int r = a + b;
		Ff = Ff & FS | r>>>8 & 0x128;
		Fa &= ~FH;
		Fb = Fb&0x80 | ((r ^ a ^ b)>>>8 ^ Fr) & FH;
		MP = a+1;
		time += 7;
		return (char)r;
	}

	private void adc_hl(int b)
	{
		int a,r;
		r = (a=HL) + b + (Ff>>>8 & FC);
		Ff = r>>>8;
		Fa = a>>>8; Fb = b>>>8;
		HL = r = (char)r;
		Fr = r>>>8 | r<<8;
		MP = a+1;
		time += 7;
	}

	private void sbc_hl(int b)
	{
		int a,r;
		r = (a=HL) - b - (Ff>>>8 & FC);
		Ff = r>>>8;
		Fa = a>>>8; Fb = ~(b>>>8);
		HL = r = (char)r;
		Fr = r>>>8 | r<<8;
		MP = a+1;
		time += 7;
	}

	private int getd(int xy)
	{
		int d = env.mem(PC);
		PC = (char)(PC+1);
		time += 8;
		return MP = (char)(xy + (byte)d);
	}

	private int imm8()
	{
		int v = env.mem(PC);
		PC = (char)(PC+1);
		time += 3;
		return v;
	}

	private int imm16()
	{
		int v = env.mem16(PC);
		PC = (char)(PC+2);
		time += 6;
		return v;
	}

	/* instructions */

	// may be wrong. see http://scratchpad.wikia.com/wiki/Z80
	private void scf_ccf(int x)
	{
		Fa &= ~FH;
		Fb = Fb&0x80 | (x>>>4 ^ Fr) & FH;
		Ff = 0x100 ^ x | Ff&FS | A&F53;
	}

	private void daa()
	{
		int h = (Fr ^ Fa ^ Fb ^ Fb>>8) & FH;

		int d = 0;
		if((A | Ff&0x100) > 0x99) d = 0x160;
		if((A&0xF | h) > 9) d += 6;

		Fa = A | 0x100; // parity
		if((Fb & 0x200)==0)
			A += (Fb = d);
		else {
			A -= d; Fb = ~d;
		}
		Ff = (Fr = A &= 0xFF) | d&0x100;
	}

	private void rrd()
	{
		int v = env.mem(HL) | A<<8;
		time += 7;
		f_szh0n0p(A = A&0xF0 | v&0x0F);
		env.mem(HL, v>>>4 & 0xFF);
		MP = HL+1;
		time += 3;
	}

	private void rld()
	{
		int v = env.mem(HL)<<4 | A&0x0F;
		time += 7;
		f_szh0n0p(A = A&0xF0 | v>>>8);
		env.mem(HL, v & 0xFF);
		MP = HL+1;
		time += 3;
	}

	private void ld_a_ir(int v)
	{
		Ff = Ff&~0xFF | (A = v);
		Fr = v==0 ? 0 : 1;
		Fa = Fb = IFF<<6 & 0x80;
		time++;
	}

	private void jp(boolean y)
	{
		int a = MP = imm16();
		if(y) PC = a;
	}

	private void jr()
	{
		int pc = PC;
		byte d = (byte)env.mem(pc); time += 8;
		MP = PC = (char)(pc+d+1);
	}

	private void call(boolean y)
	{
		int a = MP = imm16();
		if(y) {push(PC); PC = a;}
	}

	private void halt()
	{
		halted = true;
		int n = time_limit-time+3 >> 2;
		if(n>0) {
			n = env.halt(n, IR|R&0x7F);
			R+=n; time+=4*n;
		}
	}

	private void ldir(int i, boolean r)
	{
		int a,v;

		v = env.mem(a = HL); HL = (char)(a+i); time += 3;
		env.mem(a = de(), v); de((char)(a+i)); time += 5;

		if(Fr!=0) Fr = 1; // keep Z
		v += A;
		Ff = Ff&~F53 | v&F3 | v<<4&F5;

		bc(a = (char)(bc()-1));
		v = 0;
		if(a!=0) {
			if(r) {
				time += 5;
				MP = (PC = (char)(PC-2)) + 1;
			}
			v = 0x80;
		}
		Fa = Fb = v;
	}

	private void cpir(int i, boolean r)
	{
		int a,b,v;

		v = A-(b = env.mem(a=HL)) & 0xFF;
		MP += i;
		HL = (char)(a+i);
		time += 8;

		Fr = v & 0x7F | v>>>7;
		Fb = ~(b | 0x80);
		Fa = A & 0x7F;

		bc(a = (char)(bc() - 1));
		if(a!=0) {
			Fa |= 0x80;
			Fb |= 0x80;
			if(r && v!=0) {
				MP = (PC = (char)(PC-2)) + 1;
				time += 5;
			}
		}

		Ff = Ff&~0xFF | v&~F53;
		if(((v ^ b ^ A)&FH) != 0) v--;
		Ff |= v<<4&0x20 | v&8;
	}

	private void inir_otir(int op) // op: 101rd01o
	{
		int bc, hl, d, v;

		hl = (char)(HL + (d = (op&8)==0 ? 1 : -1));
		bc = B<<8|C;
		time++;
		if((op&1)==0) {
			v = env.in(bc); time += 4;
			MP = bc+d;
			bc = (char)(bc-256);
			env.mem(HL, v); time += 3;
			d += bc;
		} else {
			v = env.mem(HL); time += 3;
			bc = (char)(bc-256);
			MP = bc+d;
			env.out(bc, v); time += 4;
			d = hl;
		}
		d = (d&0xFF) + v;
		HL = hl;
		B = (bc >>= 8);
		if(op>0xB0 && bc>0) {
			time += 5;
			PC = (char)(PC-2);
		}
		int x = d&7 ^ bc;
		Ff = bc | (d &= 0x100);
		Fa = (Fr = bc) ^ 0x80;
		x = 0x4B3480 >> ((x^x>>>4)&15);
		Fb = (x^bc) & 0x80 | d>>>4 | (v & 0x80)<<2;
	}

	/* Note: EI isn't prefix here - interrupt will be acknowledged */

	void execute()
	{
		if(halted) {
			halt();
			return;
		}
		do {
			int v, c = env.m1(PC, IR|R++&0x7F);
			PC = (char)(PC+1); time += 4;
			switch(c) {
// -------------- >8 main
// case 0x00: break;
 case 0x08: ex_af(); break;
 case 0x10: {time++; v=PC; byte d=(byte)env.mem(v++); time+=3;
	if((B=B-1&0xFF)!=0) {time+=5; MP=v+=d;}
	PC=(char)v;} break;
 case 0x18: MP=PC=(char)(PC+1+(byte)env.mem(PC)); time+=8; break;
 case 0x09: HL=add16(HL,B<<8|C); break;
 case 0x19: HL=add16(HL,D<<8|E); break;
 case 0x29: HL=add16(HL,HL); break;
 case 0x39: HL=add16(HL,SP); break;
 case 0x01: v=imm16(); B=v>>>8; C=v&0xFF; break;
 case 0x11: v=imm16(); D=v>>>8; E=v&0xFF; break;
 case 0x21: HL=imm16(); break;
 case 0x23: HL=(char)(HL+1); time+=2; break;
 case 0x2B: HL=(char)(HL-1); time+=2; break;
 case 0x31: SP=imm16(); break;
 case 0x33: SP=(char)(SP+1); time+=2; break;
 case 0x3B: SP=(char)(SP-1); time+=2; break;
 case 0x03: if(++C==256) {B=B+1&0xFF;C=0;} time+=2; break;
 case 0x13: if(++E==256) {D=D+1&0xFF;E=0;} time+=2; break;
 case 0x0B: if(--C<0) B=B-1&(C=0xFF); time+=2; break;
 case 0x1B: if(--E<0) D=D-1&(E=0xFF); time+=2; break;
 case 0x02: MP=(v=B<<8|C)+1&0xFF|A<<8; env.mem(v,A); time+=3; break;
 case 0x0A: MP=(v=B<<8|C)+1; A=env.mem(v); time+=3; break;
 case 0x12: MP=(v=D<<8|E)+1&0xFF|A<<8; env.mem(v,A); time+=3; break;
 case 0x1A: MP=(v=D<<8|E)+1; A=env.mem(v); time+=3; break;
 case 0x22: MP=(v=imm16())+1; env.mem16(v,HL); time+=6; break;
 case 0x2A: MP=(v=imm16())+1; HL=env.mem16(v); time+=6; break;
 case 0x32: MP=(v=imm16())+1&0xFF|A<<8; env.mem(v,A); time+=3; break;
 case 0x3A: MP=(v=imm16())+1; A=env.mem(v); time+=3; break;
 case 0x04: B=inc(B); break;
 case 0x05: B=dec(B); break;
 case 0x06: B=imm8(); break;
 case 0x0C: C=inc(C); break;
 case 0x0D: C=dec(C); break;
 case 0x0E: C=imm8(); break;
 case 0x14: D=inc(D); break;
 case 0x15: D=dec(D); break;
 case 0x16: D=imm8(); break;
 case 0x1C: E=inc(E); break;
 case 0x1D: E=dec(E); break;
 case 0x1E: E=imm8(); break;
 case 0x24: HL=HL&0xFF|inc(HL>>>8)<<8; break;
 case 0x25: HL=HL&0xFF|dec(HL>>>8)<<8; break;
 case 0x26: HL=HL&0xFF|imm8()<<8; break;
 case 0x2C: HL=HL&0xFF00|inc(HL&0xFF); break;
 case 0x2D: HL=HL&0xFF00|dec(HL&0xFF); break;
 case 0x2E: HL=HL&0xFF00|imm8(); break;
 case 0x34: v=inc(env.mem(HL)); time+=4; env.mem(HL,v); time+=3; break;
 case 0x35: v=dec(env.mem(HL)); time+=4; env.mem(HL,v); time+=3; break;
 case 0x36: env.mem(HL,imm8()); time+=3; break;
 case 0x3C: A=inc(A); break;
 case 0x3D: A=dec(A); break;
 case 0x3E: A=imm8(); break;
 case 0x20: if(Fr!=0) jr(); else imm8(); break;
 case 0x28: if(Fr==0) jr(); else imm8(); break;
 case 0x30: if((Ff&0x100)==0) jr(); else imm8(); break;
 case 0x38: if((Ff&0x100)!=0) jr(); else imm8(); break;
 case 0x07: rot(A*0x101>>>7); break;
 case 0x0F: rot(A*0x80800000>>24); break;
 case 0x17: rot(A<<1|Ff>>>8&1); break;
 case 0x1F: rot((A*0x201|Ff&0x100)>>>1); break;
 case 0x27: daa(); break;
 case 0x2F: cpl(); break;
 case 0x37: scf_ccf(0); break;
 case 0x3F: scf_ccf(Ff&0x100); break;
// case 0x40: break;
 case 0x41: B=C; break;
 case 0x42: B=D; break;
 case 0x43: B=E; break;
 case 0x44: B=HL>>>8; break;
 case 0x45: B=HL&0xFF; break;
 case 0x46: B=env.mem(HL); time+=3; break;
 case 0x47: B=A; break;
 case 0x48: C=B; break;
// case 0x49: break;
 case 0x4A: C=D; break;
 case 0x4B: C=E; break;
 case 0x4C: C=HL>>>8; break;
 case 0x4D: C=HL&0xFF; break;
 case 0x4E: C=env.mem(HL); time+=3; break;
 case 0x4F: C=A; break;
 case 0x50: D=B; break;
 case 0x51: D=C; break;
// case 0x52: break;
 case 0x53: D=E; break;
 case 0x54: D=HL>>>8; break;
 case 0x55: D=HL&0xFF; break;
 case 0x56: D=env.mem(HL); time+=3; break;
 case 0x57: D=A; break;
 case 0x58: E=B; break;
 case 0x59: E=C; break;
 case 0x5A: E=D; break;
// case 0x5B: break;
 case 0x5C: E=HL>>>8; break;
 case 0x5D: E=HL&0xFF; break;
 case 0x5E: E=env.mem(HL); time+=3; break;
 case 0x5F: E=A; break;
 case 0x60: HL=HL&0xFF|B<<8; break;
 case 0x61: HL=HL&0xFF|C<<8; break;
 case 0x62: HL=HL&0xFF|D<<8; break;
 case 0x63: HL=HL&0xFF|E<<8; break;
// case 0x64: break;
 case 0x65: HL=HL&0xFF|(HL&0xFF)<<8; break;
 case 0x66: HL=HL&0xFF|env.mem(HL)<<8; time+=3; break;
 case 0x67: HL=HL&0xFF|A<<8; break;
 case 0x68: HL=HL&0xFF00|B; break;
 case 0x69: HL=HL&0xFF00|C; break;
 case 0x6A: HL=HL&0xFF00|D; break;
 case 0x6B: HL=HL&0xFF00|E; break;
 case 0x6C: HL=HL&0xFF00|HL>>>8; break;
// case 0x6D: break;
 case 0x6E: HL=HL&0xFF00|env.mem(HL); time+=3; break;
 case 0x6F: HL=HL&0xFF00|A; break;
 case 0x70: env.mem(HL,B); time+=3; break;
 case 0x71: env.mem(HL,C); time+=3; break;
 case 0x72: env.mem(HL,D); time+=3; break;
 case 0x73: env.mem(HL,E); time+=3; break;
 case 0x74: env.mem(HL,HL>>>8); time+=3; break;
 case 0x75: env.mem(HL,HL&0xFF); time+=3; break;
 case 0x76: halt(); break;
 case 0x77: env.mem(HL,A); time+=3; break;
 case 0x78: A=B; break;
 case 0x79: A=C; break;
 case 0x7A: A=D; break;
 case 0x7B: A=E; break;
 case 0x7C: A=HL>>>8; break;
 case 0x7D: A=HL&0xFF; break;
 case 0x7E: A=env.mem(HL); time+=3; break;
// case 0x7F: break;
 case 0xA7: Fa=~(Ff=Fr=A); Fb=0; break;
 case 0xAF: A=Ff=Fr=Fb=0; Fa=0x100; break;
 case 0x80: add(B); break;
 case 0x81: add(C); break;
 case 0x82: add(D); break;
 case 0x83: add(E); break;
 case 0x84: add(HL>>>8); break;
 case 0x85: add(HL&0xFF); break;
 case 0x86: add(env.mem(HL)); time+=3; break;
 case 0x87: add(A); break;
 case 0x88: adc(B); break;
 case 0x89: adc(C); break;
 case 0x8A: adc(D); break;
 case 0x8B: adc(E); break;
 case 0x8C: adc(HL>>>8); break;
 case 0x8D: adc(HL&0xFF); break;
 case 0x8E: adc(env.mem(HL)); time+=3; break;
 case 0x8F: adc(A); break;
 case 0x90: sub(B); break;
 case 0x91: sub(C); break;
 case 0x92: sub(D); break;
 case 0x93: sub(E); break;
 case 0x94: sub(HL>>>8); break;
 case 0x95: sub(HL&0xFF); break;
 case 0x96: sub(env.mem(HL)); time+=3; break;
 case 0x97: sub(A); break;
 case 0x98: sbc(B); break;
 case 0x99: sbc(C); break;
 case 0x9A: sbc(D); break;
 case 0x9B: sbc(E); break;
 case 0x9C: sbc(HL>>>8); break;
 case 0x9D: sbc(HL&0xFF); break;
 case 0x9E: sbc(env.mem(HL)); time+=3; break;
 case 0x9F: sbc(A); break;
 case 0xA0: and(B); break;
 case 0xA1: and(C); break;
 case 0xA2: and(D); break;
 case 0xA3: and(E); break;
 case 0xA4: and(HL>>>8); break;
 case 0xA5: and(HL&0xFF); break;
 case 0xA6: and(env.mem(HL)); time+=3; break;
 case 0xA8: xor(B); break;
 case 0xA9: xor(C); break;
 case 0xAA: xor(D); break;
 case 0xAB: xor(E); break;
 case 0xAC: xor(HL>>>8); break;
 case 0xAD: xor(HL&0xFF); break;
 case 0xAE: xor(env.mem(HL)); time+=3; break;
 case 0xB0: or(B); break;
 case 0xB1: or(C); break;
 case 0xB2: or(D); break;
 case 0xB3: or(E); break;
 case 0xB4: or(HL>>>8); break;
 case 0xB5: or(HL&0xFF); break;
 case 0xB6: or(env.mem(HL)); time+=3; break;
 case 0xB7: or(A); break;
 case 0xB8: cp(B); break;
 case 0xB9: cp(C); break;
 case 0xBA: cp(D); break;
 case 0xBB: cp(E); break;
 case 0xBC: cp(HL>>>8); break;
 case 0xBD: cp(HL&0xFF); break;
 case 0xBE: cp(env.mem(HL)); time+=3; break;
 case 0xBF: cp(A); break;
 case 0xDD:
 case 0xFD: group_xy(c); break;
 case 0xCB: group_cb(); break;
 case 0xED: group_ed(); break;
 case 0xC0: time++; if(Fr!=0) MP=PC=pop(); break;
 case 0xC2: jp(Fr!=0); break;
 case 0xC4: call(Fr!=0); break;
 case 0xC8: time++; if(Fr==0) MP=PC=pop(); break;
 case 0xCA: jp(Fr==0); break;
 case 0xCC: call(Fr==0); break;
 case 0xD0: time++; if((Ff&0x100)==0) MP=PC=pop(); break;
 case 0xD2: jp((Ff&0x100)==0); break;
 case 0xD4: call((Ff&0x100)==0); break;
 case 0xD8: time++; if((Ff&0x100)!=0) MP=PC=pop(); break;
 case 0xDA: jp((Ff&0x100)!=0); break;
 case 0xDC: call((Ff&0x100)!=0); break;
 case 0xE0: time++; if((flags()&FP)==0) MP=PC=pop(); break;
 case 0xE2: jp((flags()&FP)==0); break;
 case 0xE4: call((flags()&FP)==0); break;
 case 0xE8: time++; if((flags()&FP)!=0) MP=PC=pop(); break;
 case 0xEA: jp((flags()&FP)!=0); break;
 case 0xEC: call((flags()&FP)!=0); break;
 case 0xF0: time++; if((Ff&FS)==0) MP=PC=pop(); break;
 case 0xF2: jp((Ff&FS)==0); break;
 case 0xF4: call((Ff&FS)==0); break;
 case 0xF8: time++; if((Ff&FS)!=0) MP=PC=pop(); break;
 case 0xFA: jp((Ff&FS)!=0); break;
 case 0xFC: call((Ff&FS)!=0); break;
 case 0xC1: v=pop(); B=v>>>8; C=v&0xFF; break;
 case 0xC5: push(B<<8|C); break;
 case 0xD1: v=pop(); D=v>>>8; E=v&0xFF; break;
 case 0xD5: push(D<<8|E); break;
 case 0xE1: HL=pop(); break;
 case 0xE5: push(HL); break;
 case 0xF1: af(pop()); break;
 case 0xF5: push(A<<8|flags()); break;
 case 0xC3: MP=PC=imm16(); break;
 case 0xC6: add(imm8()); break;
 case 0xCE: adc(imm8()); break;
 case 0xD6: sub(imm8()); break;
 case 0xDE: sbc(imm8()); break;
 case 0xE6: and(imm8()); break;
 case 0xEE: xor(imm8()); break;
 case 0xF6: or(imm8()); break;
 case 0xFE: cp(imm8()); break;
 case 0xC9: MP=PC=pop(); break;
 case 0xCD: v=imm16(); push(PC); MP=PC=v; break;
 case 0xD3: env.out(v=imm8()|A<<8,A); MP=v+1&0xFF|v&0xFF00; time+=4; break;
 case 0xDB: MP=(v=imm8()|A<<8)+1; A=env.in(v); time+=4; break;
 case 0xD9: exx(); break;
 case 0xE3: v=pop(); push(HL); MP=HL=v; time+=2; break;
 case 0xE9: PC=HL; break;
 case 0xEB: v=HL; HL=D<<8|E; D=v>>>8; E=v&0xFF; break;
 case 0xF3: IFF=0; break;
 case 0xFB: IFF=3; break;
 case 0xF9: SP=HL; time+=2; break;
 case 0xC7:
 case 0xCF:
 case 0xD7:
 case 0xDF:
 case 0xE7:
 case 0xEF:
 case 0xF7:
 case 0xFF: push(PC); PC=c-199; break;
// -------------- >8
			}
		} while(time_limit - time > 0);
	}

	private void group_xy(int c0)
	{
		for(;;) {
			int xy = c0==0xDD ? IX : IY;
			int v, c = env.m1(PC, IR|R++&0x7F);
			PC = (char)(PC+1); time += 4;
			switch(c) {
// -------------- >8 xy
// case 0x00: break;
 case 0x08: ex_af(); break;
 case 0x10: time++; if((B=B-1&0xFF)!=0) jr(); else imm8(); break;
 case 0x18: jr(); break;
 case 0x09: xy=add16(xy,B<<8|C); break;
 case 0x19: xy=add16(xy,D<<8|E); break;
 case 0x29: xy=add16(xy,xy); break;
 case 0x39: xy=add16(xy,SP); break;
 case 0x01: bc(imm16()); break;
 case 0x03: bc((char)(bc()+1)); time+=2; break;
 case 0x0B: bc((char)(bc()-1)); time+=2; break;
 case 0x11: de(imm16()); break;
 case 0x13: de((char)(de()+1)); time+=2; break;
 case 0x1B: de((char)(de()-1)); time+=2; break;
 case 0x21: xy=imm16(); break;
 case 0x23: xy=(char)(xy+1); time+=2; break;
 case 0x2B: xy=(char)(xy-1); time+=2; break;
 case 0x31: SP=imm16(); break;
 case 0x33: SP=(char)(SP+1); time+=2; break;
 case 0x3B: SP=(char)(SP-1); time+=2; break;
 case 0x02: MP=(v=bc())+1&0xFF|A<<8; env.mem(v,A); time+=3; break;
 case 0x0A: MP=(v=bc())+1; A=env.mem(v); time+=3; break;
 case 0x12: MP=(v=de())+1&0xFF|A<<8; env.mem(v,A); time+=3; break;
 case 0x1A: MP=(v=de())+1; A=env.mem(v); time+=3; break;
 case 0x22: MP=(v=imm16())+1; env.mem16(v,xy); time+=6; break;
 case 0x2A: MP=(v=imm16())+1; xy=env.mem16(v); time+=6; break;
 case 0x32: MP=(v=imm16())+1&0xFF|A<<8; env.mem(v,A); time+=3; break;
 case 0x3A: MP=(v=imm16())+1; A=env.mem(v); time+=3; break;
 case 0x04: B=inc(B); break;
 case 0x05: B=dec(B); break;
 case 0x06: B=imm8(); break;
 case 0x0C: C=inc(C); break;
 case 0x0D: C=dec(C); break;
 case 0x0E: C=imm8(); break;
 case 0x14: D=inc(D); break;
 case 0x15: D=dec(D); break;
 case 0x16: D=imm8(); break;
 case 0x1C: E=inc(E); break;
 case 0x1D: E=dec(E); break;
 case 0x1E: E=imm8(); break;
 case 0x24: xy=xy&0xFF|inc(xy>>>8)<<8; break;
 case 0x25: xy=xy&0xFF|dec(xy>>>8)<<8; break;
 case 0x26: xy=xy&0xFF|imm8()<<8; break;
 case 0x2C: xy=xy&0xFF00|inc(xy&0xFF); break;
 case 0x2D: xy=xy&0xFF00|dec(xy&0xFF); break;
 case 0x2E: xy=xy&0xFF00|imm8(); break;
 case 0x34: {int a; v=inc(env.mem(a=getd(xy))); time+=4; env.mem(a,v); time+=3;} break;
 case 0x35: {int a; v=dec(env.mem(a=getd(xy))); time+=4; env.mem(a,v); time+=3;} break;
 case 0x36: {int a=(char)(xy+(byte)env.mem(PC)); time+=3;
	v=env.mem((char)(PC+1)); time+=5;
	env.mem(a,v); PC=(char)(PC+2); time+=3;} break;
 case 0x3C: A=inc(A); break;
 case 0x3D: A=dec(A); break;
 case 0x3E: A=imm8(); break;
 case 0x20: if(Fr!=0) jr(); else imm8(); break;
 case 0x28: if(Fr==0) jr(); else imm8(); break;
 case 0x30: if((Ff&0x100)==0) jr(); else imm8(); break;
 case 0x38: if((Ff&0x100)!=0) jr(); else imm8(); break;
 case 0x07: rot(A*0x101>>>7); break;
 case 0x0F: rot(A*0x80800000>>24); break;
 case 0x17: rot(A<<1|Ff>>>8&1); break;
 case 0x1F: rot((A*0x201|Ff&0x100)>>>1); break;
 case 0x27: daa(); break;
 case 0x2F: cpl(); break;
 case 0x37: scf_ccf(0); break;
 case 0x3F: scf_ccf(Ff&0x100); break;
// case 0x40: break;
 case 0x41: B=C; break;
 case 0x42: B=D; break;
 case 0x43: B=E; break;
 case 0x44: B=xy>>>8; break;
 case 0x45: B=xy&0xFF; break;
 case 0x46: B=env.mem(getd(xy)); time+=3; break;
 case 0x47: B=A; break;
 case 0x48: C=B; break;
// case 0x49: break;
 case 0x4A: C=D; break;
 case 0x4B: C=E; break;
 case 0x4C: C=xy>>>8; break;
 case 0x4D: C=xy&0xFF; break;
 case 0x4E: C=env.mem(getd(xy)); time+=3; break;
 case 0x4F: C=A; break;
 case 0x50: D=B; break;
 case 0x51: D=C; break;
// case 0x52: break;
 case 0x53: D=E; break;
 case 0x54: D=xy>>>8; break;
 case 0x55: D=xy&0xFF; break;
 case 0x56: D=env.mem(getd(xy)); time+=3; break;
 case 0x57: D=A; break;
 case 0x58: E=B; break;
 case 0x59: E=C; break;
 case 0x5A: E=D; break;
// case 0x5B: break;
 case 0x5C: E=xy>>>8; break;
 case 0x5D: E=xy&0xFF; break;
 case 0x5E: E=env.mem(getd(xy)); time+=3; break;
 case 0x5F: E=A; break;
 case 0x60: xy=xy&0xFF|B<<8; break;
 case 0x61: xy=xy&0xFF|C<<8; break;
 case 0x62: xy=xy&0xFF|D<<8; break;
 case 0x63: xy=xy&0xFF|E<<8; break;
// case 0x64: break;
 case 0x65: xy=xy&0xFF|(xy&0xFF)<<8; break;
 case 0x66: HL=HL&0xFF|env.mem(getd(xy))<<8; time+=3; break;
 case 0x67: xy=xy&0xFF|A<<8; break;
 case 0x68: xy=xy&0xFF00|B; break;
 case 0x69: xy=xy&0xFF00|C; break;
 case 0x6A: xy=xy&0xFF00|D; break;
 case 0x6B: xy=xy&0xFF00|E; break;
 case 0x6C: xy=xy&0xFF00|xy>>>8; break;
// case 0x6D: break;
 case 0x6E: HL=HL&0xFF00|env.mem(getd(xy)); time+=3; break;
 case 0x6F: xy=xy&0xFF00|A; break;
 case 0x70: env.mem(getd(xy),B); time+=3; break;
 case 0x71: env.mem(getd(xy),C); time+=3; break;
 case 0x72: env.mem(getd(xy),D); time+=3; break;
 case 0x73: env.mem(getd(xy),E); time+=3; break;
 case 0x74: env.mem(getd(xy),HL>>>8); time+=3; break;
 case 0x75: env.mem(getd(xy),HL&0xFF); time+=3; break;
 case 0x76: halt(); break;
 case 0x77: env.mem(getd(xy),A); time+=3; break;
 case 0x78: A=B; break;
 case 0x79: A=C; break;
 case 0x7A: A=D; break;
 case 0x7B: A=E; break;
 case 0x7C: A=xy>>>8; break;
 case 0x7D: A=xy&0xFF; break;
 case 0x7E: A=env.mem(getd(xy)); time+=3; break;
// case 0x7F: break;
 case 0x80: add(B); break;
 case 0x81: add(C); break;
 case 0x82: add(D); break;
 case 0x83: add(E); break;
 case 0x84: add(xy>>>8); break;
 case 0x85: add(xy&0xFF); break;
 case 0x86: add(env.mem(getd(xy))); time+=3; break;
 case 0x87: add(A); break;
 case 0x88: adc(B); break;
 case 0x89: adc(C); break;
 case 0x8A: adc(D); break;
 case 0x8B: adc(E); break;
 case 0x8C: adc(xy>>>8); break;
 case 0x8D: adc(xy&0xFF); break;
 case 0x8E: adc(env.mem(getd(xy))); time+=3; break;
 case 0x8F: adc(A); break;
 case 0x90: sub(B); break;
 case 0x91: sub(C); break;
 case 0x92: sub(D); break;
 case 0x93: sub(E); break;
 case 0x94: sub(xy>>>8); break;
 case 0x95: sub(xy&0xFF); break;
 case 0x96: sub(env.mem(getd(xy))); time+=3; break;
 case 0x97: sub(A); break;
 case 0x98: sbc(B); break;
 case 0x99: sbc(C); break;
 case 0x9A: sbc(D); break;
 case 0x9B: sbc(E); break;
 case 0x9C: sbc(xy>>>8); break;
 case 0x9D: sbc(xy&0xFF); break;
 case 0x9E: sbc(env.mem(getd(xy))); time+=3; break;
 case 0x9F: sbc(A); break;
 case 0xA0: and(B); break;
 case 0xA1: and(C); break;
 case 0xA2: and(D); break;
 case 0xA3: and(E); break;
 case 0xA4: and(xy>>>8); break;
 case 0xA5: and(xy&0xFF); break;
 case 0xA6: and(env.mem(getd(xy))); time+=3; break;
 case 0xA7: and(A); break;
 case 0xA8: xor(B); break;
 case 0xA9: xor(C); break;
 case 0xAA: xor(D); break;
 case 0xAB: xor(E); break;
 case 0xAC: xor(xy>>>8); break;
 case 0xAD: xor(xy&0xFF); break;
 case 0xAE: xor(env.mem(getd(xy))); time+=3; break;
 case 0xAF: xor(A); break;
 case 0xB0: or(B); break;
 case 0xB1: or(C); break;
 case 0xB2: or(D); break;
 case 0xB3: or(E); break;
 case 0xB4: or(xy>>>8); break;
 case 0xB5: or(xy&0xFF); break;
 case 0xB6: or(env.mem(getd(xy))); time+=3; break;
 case 0xB7: or(A); break;
 case 0xB8: cp(B); break;
 case 0xB9: cp(C); break;
 case 0xBA: cp(D); break;
 case 0xBB: cp(E); break;
 case 0xBC: cp(xy>>>8); break;
 case 0xBD: cp(xy&0xFF); break;
 case 0xBE: cp(env.mem(getd(xy))); time+=3; break;
 case 0xBF: cp(A); break;
 case 0xDD:
 case 0xFD: c0=c; continue;
 case 0xCB: group_xy_cb(xy); break;
 case 0xED: group_ed(); break;
 case 0xC0: time++; if(Fr!=0) MP=PC=pop(); break;
 case 0xC2: jp(Fr!=0); break;
 case 0xC4: call(Fr!=0); break;
 case 0xC8: time++; if(Fr==0) MP=PC=pop(); break;
 case 0xCA: jp(Fr==0); break;
 case 0xCC: call(Fr==0); break;
 case 0xD0: time++; if((Ff&0x100)==0) MP=PC=pop(); break;
 case 0xD2: jp((Ff&0x100)==0); break;
 case 0xD4: call((Ff&0x100)==0); break;
 case 0xD8: time++; if((Ff&0x100)!=0) MP=PC=pop(); break;
 case 0xDA: jp((Ff&0x100)!=0); break;
 case 0xDC: call((Ff&0x100)!=0); break;
 case 0xE0: time++; if((flags()&FP)==0) MP=PC=pop(); break;
 case 0xE2: jp((flags()&FP)==0); break;
 case 0xE4: call((flags()&FP)==0); break;
 case 0xE8: time++; if((flags()&FP)!=0) MP=PC=pop(); break;
 case 0xEA: jp((flags()&FP)!=0); break;
 case 0xEC: call((flags()&FP)!=0); break;
 case 0xF0: time++; if((Ff&FS)==0) MP=PC=pop(); break;
 case 0xF2: jp((Ff&FS)==0); break;
 case 0xF4: call((Ff&FS)==0); break;
 case 0xF8: time++; if((Ff&FS)!=0) MP=PC=pop(); break;
 case 0xFA: jp((Ff&FS)!=0); break;
 case 0xFC: call((Ff&FS)!=0); break;
 case 0xC1: bc(pop()); break;
 case 0xC5: push(bc()); break;
 case 0xD1: de(pop()); break;
 case 0xD5: push(de()); break;
 case 0xE1: xy=pop(); break;
 case 0xE5: push(xy); break;
 case 0xF1: af(pop()); break;
 case 0xF5: push(A<<8|flags()); break;
 case 0xC3: MP=PC=imm16(); break;
 case 0xC6: add(imm8()); break;
 case 0xCE: adc(imm8()); break;
 case 0xD6: sub(imm8()); break;
 case 0xDE: sbc(imm8()); break;
 case 0xE6: and(imm8()); break;
 case 0xEE: xor(imm8()); break;
 case 0xF6: or(imm8()); break;
 case 0xFE: cp(imm8()); break;
 case 0xC9: MP=PC=pop(); break;
 case 0xCD: call(true); break;
 case 0xD3: env.out(v=imm8()|A<<8,A); MP=v+1&0xFF|v&0xFF00; time+=4; break;
 case 0xDB: MP=(v=imm8()|A<<8)+1; A=env.in(v); time+=4; break;
 case 0xD9: exx(); break;
 case 0xE3: v=pop(); push(xy); MP=xy=v; time+=2; break;
 case 0xE9: PC=xy; break;
 case 0xEB: v=HL; HL=de(); de(v); break;
 case 0xF3: IFF=0; break;
 case 0xFB: IFF=3; break;
 case 0xF9: SP=xy; time+=2; break;
 case 0xC7:
 case 0xCF:
 case 0xD7:
 case 0xDF:
 case 0xE7:
 case 0xEF:
 case 0xF7:
 case 0xFF: push(PC); PC=c-199; break;
// -------------- >8
			}
			if(c0==0xDD) IX = xy; else IY = xy;
			break;
		}
	}

	private void group_ed()
	{
		int v, c = env.m1(PC, IR|R++&0x7F);
		PC = (char)(PC+1); time += 4;
		switch(c) {
// -------------- >8 ed
 case 0x47: i(A); time++; break;
 case 0x4F: r(A); time++; break;
 case 0x57: ld_a_ir(IR>>>8); break;
 case 0x5F: ld_a_ir(r()); break;
 case 0x67: rrd(); break;
 case 0x6F: rld(); break;
 case 0x40: f_szh0n0p(B=env.in(B<<8|C)); time+=4; break;
 case 0x48: f_szh0n0p(C=env.in(B<<8|C)); time+=4; break;
 case 0x50: f_szh0n0p(D=env.in(B<<8|C)); time+=4; break;
 case 0x58: f_szh0n0p(E=env.in(B<<8|C)); time+=4; break;
 case 0x60: f_szh0n0p(v=env.in(B<<8|C)); HL=HL&0xFF|v<<8; time+=4; break;
 case 0x68: f_szh0n0p(v=env.in(B<<8|C)); HL=HL&0xFF00|v; time+=4; break;
 case 0x70: f_szh0n0p(env.in(B<<8|C)); time+=4; break;
 case 0x78: MP=(v=B<<8|C)+1; f_szh0n0p(A=env.in(v)); time+=4; break;
 case 0x41: env.out(B<<8|C,B); time+=4; break;
 case 0x49: env.out(B<<8|C,C); time+=4; break;
 case 0x51: env.out(B<<8|C,D); time+=4; break;
 case 0x59: env.out(B<<8|C,E); time+=4; break;
 case 0x61: env.out(B<<8|C,HL>>>8); time+=4; break;
 case 0x69: env.out(B<<8|C,HL&0xFF); time+=4; break;
 case 0x71: env.out(B<<8|C,0); time+=4; break;
 case 0x79: MP=(v=B<<8|C)+1; env.out(v,A); time+=4; break;
 case 0x42: sbc_hl(B<<8|C); break;
 case 0x4A: adc_hl(B<<8|C); break;
 case 0x43: MP=(v=imm16())+1; env.mem16(v,B<<8|C); time+=6; break;
 case 0x4B: MP=(v=imm16())+1; v=env.mem16(v); B=v>>>8; C=v&0xFF; time+=6; break;
 case 0x52: sbc_hl(D<<8|E); break;
 case 0x5A: adc_hl(D<<8|E); break;
 case 0x53: MP=(v=imm16())+1; env.mem16(v,D<<8|E); time+=6; break;
 case 0x5B: MP=(v=imm16())+1; v=env.mem16(v); D=v>>>8; E=v&0xFF; time+=6; break;
 case 0x62: sbc_hl(HL); break;
 case 0x6A: adc_hl(HL); break;
 case 0x63: MP=(v=imm16())+1; env.mem16(v,HL); time+=6; break;
 case 0x6B: MP=(v=imm16())+1; HL=env.mem16(v); time+=6; break;
 case 0x72: sbc_hl(SP); break;
 case 0x7A: adc_hl(SP); break;
 case 0x73: MP=(v=imm16())+1; env.mem16(v,SP); time+=6; break;
 case 0x7B: MP=(v=imm16())+1; SP=env.mem16(v); time+=6; break;
 case 0x44:
 case 0x4C:
 case 0x54:
 case 0x5C:
 case 0x64:
 case 0x6C:
 case 0x74:
 case 0x7C: v=A; A=0; sub(v); break;
 case 0x45:
 case 0x4D:
 case 0x55:
 case 0x5D:
 case 0x65:
 case 0x6D:
 case 0x75:
 case 0x7D: IFF|=IFF>>1; MP=PC=pop(); break;
 case 0x46:
 case 0x4E:
 case 0x56:
 case 0x5E:
 case 0x66:
 case 0x6E:
 case 0x76:
 case 0x7E: IM = c>>3&3; break;
 case 0xA0: ldir(1,false); break;
 case 0xA8: ldir(-1,false); break;
 case 0xB0: ldir(1,true); break;
 case 0xB8: ldir(-1,true); break;
 case 0xA1: cpir(1,false); break;
 case 0xA9: cpir(-1,false); break;
 case 0xB1: cpir(1,true); break;
 case 0xB9: cpir(-1,true); break;
 case 0xA2:
 case 0xA3:
 case 0xAA:
 case 0xAB:
 case 0xB2:
 case 0xB3:
 case 0xBA:
 case 0xBB: inir_otir(c); break;
// -------------- >8
		default: System.out.println(PC+": Not emulated ED/"+c);
		}
	}

	private void group_cb()
	{
		int v, c = env.m1(PC, IR|R++&0x7F);
		PC = (char)(PC+1); time += 4;
		int o = c>>>3 & 7;
		switch(c & 0xC7) {
// -------------- >8 cb
 case 0x00: B=shifter(o,B); break;
 case 0x01: C=shifter(o,C); break;
 case 0x02: D=shifter(o,D); break;
 case 0x03: E=shifter(o,E); break;
 case 0x04: HL=HL&0xFF|shifter(o,HL>>>8)<<8; break;
 case 0x05: HL=HL&0xFF00|shifter(o,HL&0xFF); break;
 case 0x06: v=shifter(o,env.mem(HL)); time+=4; env.mem(HL,v); time+=3; break;
 case 0x07: A=shifter(o,A); break;
 case 0x40: bit(o,B); break;
 case 0x41: bit(o,C); break;
 case 0x42: bit(o,D); break;
 case 0x43: bit(o,E); break;
 case 0x44: bit(o,HL>>>8); break;
 case 0x45: bit(o,HL&0xFF); break;
 case 0x46: bit(o,env.mem(HL)); Ff=Ff&~F53|MP>>>8&F53; time+=4; break;
 case 0x47: bit(o,A); break;
 case 0x80: B=B&~(1<<o); break;
 case 0x81: C=C&~(1<<o); break;
 case 0x82: D=D&~(1<<o); break;
 case 0x83: E=E&~(1<<o); break;
 case 0x84: HL&=~(0x100<<o); break;
 case 0x85: HL&=~(1<<o); break;
 case 0x86: v=env.mem(HL)&~(1<<o); time+=4; env.mem(HL,v); time+=3; break;
 case 0x87: A=A&~(1<<o); break;
 case 0xC0: B=B|1<<o; break;
 case 0xC1: C=C|1<<o; break;
 case 0xC2: D=D|1<<o; break;
 case 0xC3: E=E|1<<o; break;
 case 0xC4: HL|=0x100<<o; break;
 case 0xC5: HL|=1<<o; break;
 case 0xC6: v=env.mem(HL)|1<<o; time+=4; env.mem(HL,v); time+=3; break;
 case 0xC7: A=A|1<<o; break;
// -------------- >8
		}
	}

	private void group_xy_cb(int xy)
	{
		int pc = PC;
		int a = MP = (char)(xy + (byte)env.mem(pc));
		time += 3;
		int c = env.mem((char)(pc+1));
		PC = (char)(pc+2);
		time += 5;
		int v = env.mem(a);
		time += 4;

		int o = c>>>3 & 7;
		switch(c&0xC0) {
			case 0x00: v = shifter(o, v); break;
			case 0x40: bit(o, v); Ff=Ff&~F53 | a>>8&F53; return;
			case 0x80: v &= ~(1<<o); break;
			case 0xC0: v |= 1<<o; break;
		}
		env.mem(a, v);
		time += 3;
		switch(c&0x07) {
			case 0: B = v; break;
			case 1: C = v; break;
			case 2: D = v; break;
			case 3: E = v; break;
			case 4: HL = HL&0x00FF | v<<8; break;
			case 5: HL = HL&0xFF00 | v; break;
			case 7: A = v; break;
		}
	}

	/* interrupts */

	private int IFF, IM;
	private boolean halted;

	boolean interrupt(int bus)
	{
		if((IFF&1)==0)
			return false;
		IFF = 0;
		halted = false;
		time += 6;
		push(PC);
		switch(IM) {
		case 0:	// IM 0
		case 1:	// IM 0
			if((bus|0x38)==0xFF) {PC=bus-199; break;}
			/* not emulated */
		case 2:	// IM 1
			PC = 0x38; break;
		case 3:	// IM 2
			PC = env.mem16(IR&0xFF00 | bus);
			time += 6;
			break;
		}
		MP=PC;
		return true;
	}

	void nmi()
	{
		IFF &= 2;
		halted = false;
		push(PC);
		time += 4;
		PC = 0x66;
	}

	void reset() {
		halted = false;
		PC = IFF = IM = 0;
		af(SP = 0xFFFF);
	}
}
